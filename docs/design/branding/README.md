# Steady app icon

[`steady-app-icon.svg`](steady-app-icon.svg) is the canonical source for
Steady's app mark until Android launcher resources are implemented.

## Concept

The mark reads first as one growing leaf and second as a quiet checkmark. The
leaf communicates daily return and gradual growth; its single broad vein bends
into a check to suggest progress without the urgency of a task-management,
finance, or fitness badge.

## Geometry

- The master is a `1080 × 1080` SVG with no text, gradients, filters, or small
  decorative details.
- A broad asymmetric leaf leans upward and right. Its rounded, open silhouette
  avoids the common symmetrical stock-leaf shape.
- The light central vein is one heavy, round-ended path. Its short descending
  entry and longer rising stem create the secondary checkmark reading.
- The essential leaf and vein stay inside the central adaptive-icon safe area.
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
- Foreground layer: derive the halo, leaf, and vein from this SVG without
  changing their relative geometry. Keep the leaf/check inside the adaptive
  icon's central safe zone so it survives circle, rounded-square, squircle, and
  vendor masks.
- Do not bake in a launcher mask, add a border, shrink the mark to compensate
  for one device, or add gradients and shadow detail.
- Verify the generated foreground at small launcher sizes and with Android's
  parallax allowance before promoting resources into `app/src/main/res`.

## Monochrome

For a future Android themed icon, use the leaf silhouette and vein relationship
as a single-color mask: render the leaf as the solid shape and the vein as
negative space. The halo and warm background are omitted. Test the cutout at
small sizes before shipping because the vein must remain visibly open.
