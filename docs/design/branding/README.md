# Steady app icon

[`steady-app-icon.svg`](steady-app-icon.svg) is the canonical source for
Steady's app mark until Android launcher resources are implemented.

## Concept

The mark is a small line-art sapling with two open leaves and a quiet checkmark
near its base. The sapling communicates a routine taking root through daily
return; the secondary check suggests progress without the urgency of a
task-management, finance, or fitness badge.

## Geometry

- The master is a `1080 × 1080` SVG with no text, gradients, filters, or small
  decorative details.
- Two open leaf contours meet a simple central stem. Rounded line endings keep
  the mark quiet and human rather than technical.
- A small check sits near the base and visually connects with the stem without
  becoming the primary reading.
- The essential sapling and check stay inside the central adaptive-icon safe area.
  The circular halo is supportive and may be cropped without harming
  recognition.

## Color

Light presentation:

- Warm neutral background: `#F7F5EF`
- Light sage halo and vein: `#DCE9E1`
- Deep sage leaf: `#426B5A`

Dark presentation:

- Warm green-black background: `#151A17`
- Dark sage halo and vein: `#24352D`
- Light sage leaf: `#9BC8B0`

The SVG responds to `prefers-color-scheme` for documentation preview. Android
should later use explicit light/dark semantic resources rather than depending
on SVG media queries.

## Adaptive icon guidance

- Background layer: a full-bleed solid warm neutral (`#F7F5EF` light,
  `#151A17` dark).
- Foreground layer: derive the halo, sapling, and check from this SVG without
  changing their relative geometry. Keep the leaf/check inside the adaptive
  icon's central safe zone so it survives circle, rounded-square, squircle, and
  vendor masks.
- Do not bake in a launcher mask, add a border, shrink the mark to compensate
  for one device, or add gradients and shadow detail.
- Verify the generated foreground at small launcher sizes and with Android's
  parallax allowance before promoting resources into `app/src/main/res`.

## Monochrome

For a future Android themed icon, use the sapling and check strokes as a
single-color mask. The halo and warm background are omitted. Test the stroke
weight at small sizes before shipping so both leaves and the check remain open.
