import unittest

from release_identity import (
    MAX_VERSION_CODE,
    ReleaseIdentity,
    SemanticVersion,
    ValidationError,
    parse_release_identity,
    validate_production_tag,
    validate_pull_request_identity,
)


def identity(version_name: str, version_code: int) -> ReleaseIdentity:
    return ReleaseIdentity(
        version_name=SemanticVersion.parse(version_name, "test fixture"),
        version_code=version_code,
    )


class ReleaseIdentityParsingTest(unittest.TestCase):
    def test_parses_valid_release_identity(self) -> None:
        parsed = parse_release_identity("VERSION_NAME=1.10.0\nVERSION_CODE=42\n")

        self.assertEqual(identity("1.10.0", 42), parsed)

    def test_rejects_missing_properties(self) -> None:
        for contents, expected_message in (
            ("VERSION_CODE=3\n", "Missing VERSION_NAME"),
            ("VERSION_NAME=1.0.0\n", "Missing VERSION_CODE"),
        ):
            with self.subTest(contents=contents):
                with self.assertRaisesRegex(ValidationError, expected_message):
                    parse_release_identity(contents)

    def test_rejects_malformed_strict_semver(self) -> None:
        for version_name in ("1.0", "v1.0.0", "1.0.0-beta", "01.0.0"):
            with self.subTest(version_name=version_name):
                with self.assertRaisesRegex(ValidationError, "strict numeric SemVer"):
                    parse_release_identity(
                        f"VERSION_NAME={version_name}\nVERSION_CODE=3\n"
                    )

    def test_rejects_invalid_version_codes(self) -> None:
        for version_code in ("three", "-1", "0", str(MAX_VERSION_CODE + 1)):
            with self.subTest(version_code=version_code):
                with self.assertRaisesRegex(ValidationError, "Invalid VERSION_CODE"):
                    parse_release_identity(
                        f"VERSION_NAME=1.0.0\nVERSION_CODE={version_code}\n"
                    )


class PullRequestIdentityValidationTest(unittest.TestCase):
    def test_accepts_unchanged_identity_even_after_its_production_tag_exists(self) -> None:
        current = identity("1.0.0", 3)

        validate_pull_request_identity(current, current, {"v1.0.0"})

    def test_accepts_higher_code_and_non_decreasing_semver(self) -> None:
        validate_pull_request_identity(
            identity("1.9.0", 3),
            identity("1.10.0", 4),
            set(),
        )

    def test_accepts_same_unreleased_name_with_higher_code(self) -> None:
        validate_pull_request_identity(
            identity("1.0.0", 3),
            identity("1.0.0", 4),
            set(),
        )

    def test_rejects_non_increasing_code_when_identity_changes(self) -> None:
        with self.assertRaisesRegex(ValidationError, "must increase VERSION_CODE"):
            validate_pull_request_identity(
                identity("1.0.0", 3),
                identity("1.1.0", 3),
                set(),
            )

    def test_rejects_semver_regression(self) -> None:
        with self.assertRaisesRegex(ValidationError, "must not decrease VERSION_NAME"):
            validate_pull_request_identity(
                identity("2.0.0", 3),
                identity("1.99.99", 4),
                set(),
            )

    def test_rejects_reuse_of_a_tagged_version_name(self) -> None:
        with self.assertRaisesRegex(ValidationError, "already released as immutable tag"):
            validate_pull_request_identity(
                identity("1.0.0", 3),
                identity("1.0.0", 4),
                {"v1.0.0"},
            )


class ProductionTagValidationTest(unittest.TestCase):
    def test_accepts_matching_tag_on_main(self) -> None:
        validate_production_tag("v1.0.0", identity("1.0.0", 3), True)

    def test_rejects_malformed_tag(self) -> None:
        with self.assertRaisesRegex(ValidationError, "Invalid production tag"):
            validate_production_tag("v1.0", identity("1.0.0", 3), True)

    def test_rejects_tag_that_does_not_match_committed_version_name(self) -> None:
        with self.assertRaisesRegex(ValidationError, "does not match VERSION_NAME"):
            validate_production_tag("v1.1.0", identity("1.0.0", 3), True)

    def test_rejects_tag_outside_main_history(self) -> None:
        with self.assertRaisesRegex(ValidationError, "belonging to main"):
            validate_production_tag("v1.0.0", identity("1.0.0", 3), False)


if __name__ == "__main__":
    unittest.main()
