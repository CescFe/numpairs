#!/usr/bin/env python3

from __future__ import annotations

import argparse
from dataclasses import dataclass
from pathlib import Path
import re
import subprocess
import sys
from typing import Collection, Sequence


VERSION_FILE = Path("version.properties")
MAX_VERSION_CODE = 2_100_000_000
SEMVER_PATTERN = re.compile(r"(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)")
TAG_PATTERN = re.compile(r"v(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)")


class ValidationError(ValueError):
    pass


@dataclass(frozen=True, order=True)
class SemanticVersion:
    major: int
    minor: int
    patch: int

    @classmethod
    def parse(cls, value: str, source: str) -> SemanticVersion:
        match = SEMVER_PATTERN.fullmatch(value)
        if match is None:
            raise ValidationError(
                f"Invalid VERSION_NAME '{value}' in {source}. "
                "Expected strict numeric SemVer in MAJOR.MINOR.PATCH form, such as 1.0.0."
            )
        return cls(*(int(component) for component in match.groups()))

    def __str__(self) -> str:
        return f"{self.major}.{self.minor}.{self.patch}"


@dataclass(frozen=True)
class ReleaseIdentity:
    version_name: SemanticVersion
    version_code: int

    def __str__(self) -> str:
        return f"{self.version_name} ({self.version_code})"


def parse_release_identity(contents: str, source: str = str(VERSION_FILE)) -> ReleaseIdentity:
    properties: dict[str, str] = {}
    for line_number, raw_line in enumerate(contents.splitlines(), start=1):
        line = raw_line.strip()
        if not line or line.startswith(("#", "!")):
            continue
        if "=" not in line:
            raise ValidationError(
                f"Malformed property at {source}:{line_number}. Expected KEY=VALUE."
            )
        key, value = (part.strip() for part in line.split("=", maxsplit=1))
        if key in properties:
            raise ValidationError(f"Duplicate {key} in {source}.")
        properties[key] = value

    version_name_value = properties.get("VERSION_NAME")
    if not version_name_value:
        raise ValidationError(
            f"Missing VERSION_NAME in {source}. Set it to strict numeric SemVer such as 1.0.0."
        )

    version_code_value = properties.get("VERSION_CODE")
    if not version_code_value:
        raise ValidationError(
            f"Missing VERSION_CODE in {source}. Set it to a positive integer."
        )
    if re.fullmatch(r"\d+", version_code_value) is None:
        raise ValidationError(
            f"Invalid VERSION_CODE '{version_code_value}' in {source}. "
            "Expected a positive Google Play integer."
        )
    version_code = int(version_code_value)
    if version_code <= 0 or version_code > MAX_VERSION_CODE:
        raise ValidationError(
            f"Invalid VERSION_CODE '{version_code_value}' in {source}. "
            f"Expected an integer between 1 and {MAX_VERSION_CODE}."
        )

    return ReleaseIdentity(
        version_name=SemanticVersion.parse(version_name_value, source),
        version_code=version_code,
    )


def validate_pull_request_identity(
    base: ReleaseIdentity,
    candidate: ReleaseIdentity,
    production_tags: Collection[str],
) -> None:
    if candidate == base:
        return

    if candidate.version_code <= base.version_code:
        raise ValidationError(
            "A Pull Request that changes app release identity must increase VERSION_CODE: "
            f"base is {base.version_code}, candidate is {candidate.version_code}."
        )
    if candidate.version_name < base.version_name:
        raise ValidationError(
            "A Pull Request must not decrease VERSION_NAME: "
            f"base is {base.version_name}, candidate is {candidate.version_name}."
        )

    candidate_tag = f"v{candidate.version_name}"
    if candidate_tag in production_tags:
        raise ValidationError(
            f"VERSION_NAME {candidate.version_name} is already released as immutable tag "
            f"{candidate_tag}. Choose a new SemVer release name."
        )


def validate_production_tag(
    tag_name: str,
    identity: ReleaseIdentity,
    belongs_to_main: bool,
) -> None:
    match = TAG_PATTERN.fullmatch(tag_name)
    if match is None:
        raise ValidationError(
            f"Invalid production tag '{tag_name}'. Expected vMAJOR.MINOR.PATCH, such as v1.0.0."
        )

    tagged_version = SemanticVersion(*(int(component) for component in match.groups()))
    if tagged_version != identity.version_name:
        raise ValidationError(
            f"Production tag {tag_name} does not match VERSION_NAME {identity.version_name} "
            "in the tagged revision."
        )
    if not belongs_to_main:
        raise ValidationError(
            f"Production tag {tag_name} does not identify a revision belonging to main."
        )


def run_git(
    *arguments: str,
    allow_exit_codes: Collection[int] = (0,),
) -> subprocess.CompletedProcess[str]:
    result = subprocess.run(
        ["git", *arguments],
        check=False,
        capture_output=True,
        text=True,
    )
    if result.returncode not in allow_exit_codes:
        details = result.stderr.strip() or result.stdout.strip() or "unknown Git error"
        raise ValidationError(f"Git command failed: git {' '.join(arguments)}: {details}")
    return result


def read_worktree_identity() -> ReleaseIdentity:
    try:
        contents = VERSION_FILE.read_text(encoding="utf-8")
    except FileNotFoundError as error:
        raise ValidationError(f"Missing app version contract at {VERSION_FILE}.") from error
    return parse_release_identity(contents)


def read_revision_identity(revision: str) -> ReleaseIdentity:
    result = run_git("show", f"{revision}:{VERSION_FILE.as_posix()}")
    return parse_release_identity(result.stdout, f"{VERSION_FILE} at {revision}")


def repository_tags() -> set[str]:
    return set(run_git("tag", "--list").stdout.splitlines())


def tag_commit(tag_name: str) -> str:
    result = run_git("rev-parse", "--verify", f"refs/tags/{tag_name}^{{commit}}")
    return result.stdout.strip()


def revision_belongs_to(commit: str, main_revision: str) -> bool:
    result = run_git(
        "merge-base",
        "--is-ancestor",
        commit,
        main_revision,
        allow_exit_codes=(0, 1),
    )
    return result.returncode == 0


def validate_current_command(_: argparse.Namespace) -> None:
    identity = read_worktree_identity()
    print(f"Validated app release identity {identity}.")


def validate_pull_request_command(arguments: argparse.Namespace) -> None:
    base = read_revision_identity(arguments.base_revision)
    candidate = read_worktree_identity()
    validate_pull_request_identity(base, candidate, repository_tags())
    print(f"Validated Pull Request app release identity {candidate} against base {base}.")


def validate_tag_command(arguments: argparse.Namespace) -> None:
    commit = tag_commit(arguments.tag)
    identity = read_revision_identity(commit)
    validate_production_tag(
        arguments.tag,
        identity,
        revision_belongs_to(commit, arguments.main_revision),
    )
    print(f"Validated production tag {arguments.tag} for app release identity {identity}.")


def create_argument_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description="Validate the NumPairs app release identity.")
    subparsers = parser.add_subparsers(dest="command", required=True)

    current_parser = subparsers.add_parser("current", help="Validate the working-tree identity.")
    current_parser.set_defaults(handler=validate_current_command)

    pull_request_parser = subparsers.add_parser(
        "pull-request",
        help="Validate the working-tree identity against a base revision.",
    )
    pull_request_parser.add_argument("--base-revision", required=True)
    pull_request_parser.set_defaults(handler=validate_pull_request_command)

    tag_parser = subparsers.add_parser(
        "tag",
        help="Validate a production tag against its committed identity and main.",
    )
    tag_parser.add_argument("--tag", required=True)
    tag_parser.add_argument("--main-revision", default="origin/main")
    tag_parser.set_defaults(handler=validate_tag_command)

    return parser


def main(arguments: Sequence[str] | None = None) -> int:
    parser = create_argument_parser()
    parsed_arguments = parser.parse_args(arguments)
    try:
        parsed_arguments.handler(parsed_arguments)
    except ValidationError as error:
        print(f"Release identity validation failed: {error}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
