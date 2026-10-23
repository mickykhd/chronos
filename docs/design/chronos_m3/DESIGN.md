---
name: Chronos M3
colors:
  surface: '#121416'
  surface-dim: '#121416'
  surface-bright: '#37393b'
  surface-container-lowest: '#0c0e10'
  surface-container-low: '#1a1c1e'
  surface-container: '#1e2022'
  surface-container-high: '#282a2c'
  surface-container-highest: '#333537'
  on-surface: '#e2e2e5'
  on-surface-variant: '#d7c3b6'
  inverse-surface: '#e2e2e5'
  inverse-on-surface: '#2f3133'
  outline: '#9f8d82'
  outline-variant: '#52443b'
  surface-tint: '#ffb782'
  primary: '#ffdcc5'
  on-primary: '#4f2500'
  primary-container: '#ffb782'
  on-primary-container: '#79461b'
  inverse-primary: '#875125'
  secondary: '#e7bdb1'
  on-secondary: '#442a22'
  secondary-container: '#604239'
  on-secondary-container: '#d8afa4'
  tertiary: '#f5e1a7'
  on-tertiary: '#3b2f05'
  tertiary-container: '#d8c58d'
  on-tertiary-container: '#5f5124'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#ffdcc5'
  primary-fixed-dim: '#ffb782'
  on-primary-fixed: '#301400'
  on-primary-fixed-variant: '#6b3a10'
  secondary-fixed: '#ffdbd0'
  secondary-fixed-dim: '#e7bdb1'
  on-secondary-fixed: '#2c150e'
  on-secondary-fixed-variant: '#5d4037'
  tertiary-fixed: '#f5e1a7'
  tertiary-fixed-dim: '#d8c58d'
  on-tertiary-fixed: '#231b00'
  on-tertiary-fixed-variant: '#53461a'
  background: '#121416'
  on-background: '#e2e2e5'
  surface-variant: '#333537'
typography:
  display-hero:
    fontFamily: Roboto Flex
    fontSize: 96px
    fontWeight: '300'
    lineHeight: 96px
    letterSpacing: -0.04em
  display-hero-mobile:
    fontFamily: Roboto Flex
    fontSize: 64px
    fontWeight: '300'
    lineHeight: 68px
    letterSpacing: -0.03em
  headline-lg:
    fontFamily: Roboto Flex
    fontSize: 44px
    fontWeight: '400'
    lineHeight: 52px
    letterSpacing: -0.02em
  headline-lg-mobile:
    fontFamily: Roboto Flex
    fontSize: 36px
    fontWeight: '400'
    lineHeight: 44px
    letterSpacing: -0.02em
  headline-md:
    fontFamily: Roboto Flex
    fontSize: 28px
    fontWeight: '500'
    lineHeight: 36px
    letterSpacing: 0em
  headline-sm:
    fontFamily: Roboto Flex
    fontSize: 22px
    fontWeight: '500'
    lineHeight: 28px
    letterSpacing: 0em
  title-lg:
    fontFamily: Roboto Flex
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 24px
    letterSpacing: 0em
  title-md:
    fontFamily: Roboto Flex
    fontSize: 16px
    fontWeight: '500'
    lineHeight: 22px
    letterSpacing: 0.01em
  body-lg:
    fontFamily: Roboto Flex
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
    letterSpacing: 0.01em
  body-md:
    fontFamily: Roboto Flex
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
    letterSpacing: 0.02em
  label-lg:
    fontFamily: Roboto Flex
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 20px
    letterSpacing: 0.02em
  label-md:
    fontFamily: Roboto Flex
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
    letterSpacing: 0.04em
  label-sm:
    fontFamily: Roboto Flex
    fontSize: 11px
    fontWeight: '500'
    lineHeight: 14px
    letterSpacing: 0.05em
rounded:
  sm: 0.5rem
  DEFAULT: 1rem
  md: 1.5rem
  lg: 2rem
  xl: 3rem
  full: 9999px
spacing:
  gutter: 1rem
  gutter-mobile: 0.75rem
  margin: 1.5rem
  margin-mobile: 1rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 1rem
  space-lg: 1.5rem
  space-xl: 2.5rem
---

## Brand & Style

This design system models the expressive, kinetic aesthetic of modern Material 3 on contemporary mobile devices. Designed for fluid utility, focused nighttime readability, and ambient delight, it couples high-contrast OLED black surfaces with luminous, warm accents.

### Core Character & Mood
- **Atmospheric & Night-Optimized:** Built around true OLED dark foundations to prevent glare in bedroom environments while conserving display energy.
- **Expressive Tactility:** Large, friendly interactive targets with pill contours, dynamic color tonal pairing, and exaggerated visual hierarchy.
- **Fluid & Temporal:** Smooth motion metaphors, circular sweep timers, and oversized digital numerals that celebrate the passing of time.
- **Material 3 Expressive:** Embraces high-volume asymmetry, tonal container nesting, and geometric clarity characteristic of Android 14 and 15 system applications.

## Colors

The palette is tuned around a radiant amber-coral dynamic key color interacting with layered obsidian-slate dark neutrals. This ensures zero optical fatigue in pitch-black conditions while delivering punchy urgency for alarms and timers.

### Surface Architecture & Tonal Containers
- **Base Canvas (`#121316`):** Pure deep slate-black background for maximum battery efficiency and zero edge bleed on OLED panels.
- **Surface Container Low (`#1A1C1E`):** Primary tier for inactive alarm cards, background rings, and grouped chronometer sections.
- **Surface Container High (`#282A2D`):** Elevated tier for active item states, floating controls, dialogs, and interactive dial plates.
- **Surface Container Highest (`#333539`):** Subtle highlight tier for unselected tab pills, slider tracks, and divider surfaces.

### Accent Roles
- **Primary (`#FFB782` - Warm Amber/Apricot):** Focal controls, active FAB, primary digits in focus, running stopwatch elapsed indicators.
- **Secondary (`#E7BDB1` - Soft Terracotta/Coral):** Selected navigation tab indicators, secondary countdown labels, and auxiliary chips.
- **Tertiary (`#D8C58D` - Sunlit Brass):** Active toggle tracks, lap delta markers, and secondary notification badges.
- **Text & Glyphs:** `#EDE0DC` (On-Surface primary, high legibility) and `#8D9199` (On-Surface variant, low prominence).

## Typography

The type system is powered entirely by Roboto Flex, calibrated to evoke modern stock Android clock faces. It delivers proportional structural discipline along with expressive tabular numeric readout.

### Numeric Readouts & Time Rendering
- Numerical readouts on the Timer, Clock, and Stopwatch must use OpenType tabular figures (`font-variant-numeric: tabular-nums`) to prevent horizontal jitter during rapid millisecond or second increments.
- The `display-hero` styles are reserved for the central timer countdown and primary alarm clocks.
- Thin weight (`300`) applied to ultra-large numerals gives an airy, elegant aesthetic without encroaching on layout density.

## Layout & Spacing

Layouts follow a fluid, device-adapting single-canvas model anchored by bottom-weighted thumb-zone ergonomics.

### Screen Partitioning & Thumb Reach
- **Content Canvas:** Aligns to a responsive 4-column structure on phone devices and an 8-column layout on foldable or tablet displays.
- **Bottom Navigation Dock:** Fixed-height (80px) floating or flush pill-dock anchored above the system gesture inset.
- **Top Canvas (The "Glance" Zone):** Holds current active time, world clock horizons, or expansive circular progress gauges, spaced away from camera cutouts by generous top section margins (`space-xl`).
- **Reflow Rules:** On wide or unfolded screens, the alarm and world-clock lists split into a balanced 2-column masonry grid, while the circular timer gauge anchors to the left half and controls take the right half.

## Elevation & Depth

Consistent with Material 3 Expressive, elevation is communicated through **tonal color shifts** rather than aggressive drop shadows.

### Surface Tiers
- **Tier 0 (Base Canvas - `#121316`):** The default screen background. Inset, non-clickable canvas area.
- **Tier 1 (Surface Container Low - `#1A1C1E`):** Standard non-elevated cards, inactive alarm items, and day picker buttons. Uses a sharp 1px interior ghost outline (`rgba(255, 255, 255, 0.04)`) to maintain edge definition against the pitch canvas.
- **Tier 2 (Surface Container High - `#282A2D`):** Active alarms, running timers, open bottom sheets, and popovers. Employs an ultra-subtle tinted ambient glow: `0 8px 24px -4px rgba(0, 0, 0, 0.60)`.
- **Tier 3 (Floating Action & Modals):** Primary FAB and floating bottom navigation. Employs a dual shadow with a warm ambient bounce: `0 12px 32px -6px rgba(0, 0, 0, 0.7), 0 4px 12px 0px rgba(255, 183, 130, 0.08)`.

## Shapes

The design system embraces Material 3's high-radius curvature. Geometry shifts intentionally between squircle-curved containers and full capsule/pill shapes.

### Geometric Rules
- **Pills (`rounded-full` / 9999px):** Universal treatment for action buttons, active navigation indicator chips, day-of-week selectors, and quick-add time chips (+1m, +5m).
- **Cards & Sheets (`rounded-xl` / 2rem / 32px):** Applied to alarm cards, modal bottom sheets, time-picker dialogue frames, and lap tables.
- **Circular Indicators:** Full 360-degree SVG rings with rounded caps (`stroke-linecap: round`) for the elapsed timer and stopwatch visualizations.

## Components

### Floating Action Button (FAB)
- **Geometry:** 64x64px squircle (24px radius) for standard FAB; expandable pill (height 56px, horizontal padding `space-lg`) for extended action FABs.
- **Color:** Filled with Primary Accent (`#FFB782`), icon rendered in `#4D2700`.
- **Behavior:** Sits centered or bottom-right trailing, resting 16px above the navigation dock. On scroll down, it collapses from extended to standard icon-only format.

### Navigation Bar (Fluid Bottom Tabs)
- **Structure:** 4-tab horizontal dock (Alarm, Clock, Timer, Stopwatch).
- **Active State:** A filled pill capsule (`#E7BDB1` at 20% opacity or solid `#422A23`) encapsulating the active icon in `#FFB782`, accompanied by `label-md` text.
- **Inactive State:** Unadorned glyph in `#8D9199`, no background pill.

### Alarm & World Clock Cards
- **Architecture:** `Surface Container Low` background with 28px border-radius.
- **Layout:** High-contrast time display (`headline-lg` in `#EDE0DC`) on the left, paired with sub-labels for repeating days and label names. Right-aligned toggle switch.
- **Active vs. Inactive State:** When toggled off, numerical text drops to 38% opacity (`#6E727A`) and days lose their amber highlight.

### Circular Progress Ring
- **Visuals:** Dual-ring presentation. Background ring rendered in `#282A2D` at 8px stroke weight. Foreground animated ring rendered in solid `#FFB782` or gradient-to-`#E7BDB1` with rounded stroke termination.
- **Interior Area:** Houses the responsive `display-hero` remaining-time readout with stacked play/pause/reset pill buttons underneath.

### Toggle Switches (Material 3 Spec)
- **Track:** 52px width, 32px height, full pill shape. Active track in `#FFB782`; inactive track in `#282A2D` with 2px stroke in `#484B50`.
- **Thumb:** 24px circle. Active thumb in `#4D2700` (contracting/expanding slightly during touch-down interaction); inactive thumb in `#8D9199`.

### Chips & Quick Adders
- **Style:** Compact pill buttons (height 36px) used above number pads or inside timer screens (e.g., `+1:00`, `+5:00`).
- **Surface:** `#282A2D` background, `#EDE0DC` label text, scaling down to 96% scale on press with haptic feedback.

### Lap Table (Stopwatch)
- **Row Styling:** Tabular numeric readouts split into columns: Lap Number, Split Time, Total Time.
- **Highlighting:** Fastest lap highlighted in tertiary brass (`#D8C58D`), slowest lap marked in muted crimson (`#E06C6C`).