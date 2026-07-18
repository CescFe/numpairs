# ADR-004: Keep v9 Platform Branding Static

## Context

NumPairs v9 introduces a persisted color preference with five themes:

- Warm
- Frost
- Obsidian
- Terminal
- Ember

Compose content can observe that preference and update in-app colors at runtime. Android's
system splash and launcher, however, do not render from the Compose tree.

The v9 investigation needs to determine whether the selected theme can also control:

- the starting-window background and symbol
- the adaptive, round, and legacy launcher icon
- the monochrome layer used by system-themed icons

The solution must keep the existing DataStore value as the only authoritative theme
preference, never block startup on unbounded work, preserve a safe fallback, and avoid
unreliable component-identity changes.

Relevant platform contracts:

- Android shows the system splash before the application is ready and defines its elements
  through manifest theme resources. See
  [Splash screens](https://developer.android.com/develop/ui/views/launch/splash-screen).
- `installSplashScreen()` must run before `super.onCreate()`, but the starting window has
  already been selected from the launching activity theme. See
  [Migrate your splash screen implementation](https://developer.android.com/develop/ui/views/launch/splash-screen/migrate).
- An
  [`activity-alias`](https://developer.android.com/guide/topics/manifest/activity-alias-element)
  can own launcher intent filters and an icon, but it cannot declare an independent activity
  theme.
- Component states can be changed through
  [`PackageManager`](https://developer.android.com/reference/android/content/pm/PackageManager).
  Atomic multi-component changes are available only from API 33, and the platform warns that
  `DONT_KILL_APP` can make application behavior unpredictable.
- A launcher can render adaptive icons in different shapes and, when system-themed icons are
  enabled, controls the tint applied to the monochrome layer. See
  [Adaptive icons](https://developer.android.com/develop/ui/compose/system/icon_design_adaptive).

## Options Considered

### Resolve DataStore Before Installing the Splash Screen

Read the selected theme at the beginning of `MainActivity.onCreate()`, choose a theme, and
then install the splash screen.

This does not change the system starting window that Android already created from the
manifest theme. A synchronous or blocking DataStore read would also add startup latency and
still would not solve cold-start selection. Mirroring the preference into a second
synchronous store would create two authoritative values and possible divergence.

### Provide Per-Theme Splash Resources to One Entry Activity

Static Frost, Obsidian, Terminal, and Ember splash styles are straightforward to define, but
Android resource selection has no qualifier for a NumPairs DataStore preference. One entry
activity therefore cannot select one of those resources early enough for the system starting
window.

### Switch Launcher Aliases and Theme-Specific Entry Activities

An alias can expose its own launcher icon, but its manifest contract has no `android:theme`
attribute. Theme-specific splash behavior would consequently require separate target entry
activities, plus one launcher alias per theme.

Switching those aliases would make the enabled launcher component a derived projection of
the DataStore preference. The approach has several unresolved risks:

- before API 33, enabling one component and disabling another is not atomic
- using `DONT_KILL_APP` is explicitly documented as potentially unpredictable
- launcher refresh timing and icon caching are launcher-controlled
- existing recent-task and pinned-shortcut references can retain the previous component
  identity or become unavailable
- a process interruption between preference persistence and component reconciliation can
  temporarily leave the two states out of sync
- NumPairs cannot verify the required launcher and lifecycle matrix without device testing

The DataStore value could remain authoritative and repair aliases on the next launch, but
that does not remove the component-identity, refresh, recents, or shortcut risks.

### Keep Platform Branding Static

Keep the existing Warm splash and launcher resources as the stable platform identity. Apply
the selected palette to in-app branding only. Continue providing adaptive, round, legacy,
and monochrome assets with unchanged NumPairs geometry.

This does not reach the aspirational selected-theme splash or launcher behavior, but it is
the only option that meets the reliable-startup and stable-component requirements for v9.

## Decision

NumPairs v9 will keep platform branding static and Warm:

- the system splash always uses the existing Warm background and NumPairs symbol
- adaptive, round, and legacy launcher icons remain the existing Warm assets
- the adaptive icon continues to expose a valid monochrome NumPairs layer
- Android and the active launcher control system-themed-icon tint
- the selected NumPairs theme controls in-app branding, surfaces, and brand colors only
- DataStore remains the single source of truth for the selected theme
- no startup path waits synchronously for personalization preferences
- corrupt or unavailable theme data continues to fall back to Warm

No activity aliases, theme-specific entry activities, or `PackageManager` component
switching will be introduced in v9.

## Supported Behavior

| Context | v9 behavior |
| --- | --- |
| Cold start | Android shows the static Warm splash before application preference loading. |
| Warm start | If Android creates a starting window, it uses the same static Warm resources. |
| Hot start | Android shows no splash; the current in-app theme remains visible. |
| In-app branding | The NumPairs mark keeps its geometry and follows the selected appearance palette. |
| Adaptive launcher | The static Warm foreground and background are exposed to supporting launchers. |
| Round or legacy launcher | The static Warm packaged bitmap is used. |
| System-themed icon | The launcher tints the NumPairs monochrome layer from the user's system palette. |
| Missing or corrupt preference | NumPairs safely falls back to Warm without delaying launch. |

Launcher cache timing, masking, visual effects, and themed tint are outside application
control. NumPairs therefore does not promise that its launcher appearance matches the
selected in-app theme.

## Consequences

### Positive

- Startup remains deterministic and non-blocking.
- Personalization retains one authoritative persisted value.
- The launcher component identity stays stable across theme changes.
- Existing recent tasks and shortcuts are not invalidated by component switching.
- Warm provides a recognizable fallback before Compose renders the selected theme.
- Monochrome system-themed icon support remains valid without claiming control over tint.

### Negative

- Frost, Obsidian, Terminal, and Ember do not change the system splash.
- The launcher icon does not follow the selected NumPairs theme.
- A non-Warm selection can transition from a Warm splash to the selected in-app palette.

## Future Considerations

Reconsider this decision only when at least one of these conditions is met:

- Android exposes a supported preference-aware starting-window or per-app launcher-icon API
  that does not require component switching
- NumPairs establishes a representative device and launcher test matrix for alias lifecycle,
  process restoration, recents, shortcuts, and icon refresh
- product requirements accept launcher-specific behavior and the operational cost of
  multiple entry components

Any future change must preserve the DataStore preference as the single source of truth,
retain a Warm or neutral fallback, and keep the system-controlled monochrome tint explicit.
