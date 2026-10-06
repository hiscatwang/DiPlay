# Universal build branding (2026-10-06)

This universal build uses the existing green DiPlay icon from
`common/src/main/res/drawable/ic_carplay.png`, copied without changes to
`common/src/main/res/raw/ic_car_home.png`. Its return-to-car label is "返回车机".
The ORA raw image described below is no longer packaged in this build.
Existing upstream asset ownership and licence notices still apply.

The following notes describe the earlier ORA-specific build for provenance.

# ORA return-to-car icon

The ORA compatibility build uses the brand's published square image for CarPlay's
return-to-car entry, labelled "欧拉".

- Official website: https://www.oraev.com/
- Image: https://www.oraev.com/dist/statics/images/share/logo.jpg
- Retrieved: 2026-10-04
- Packaged resource: `common/src/main/res/raw/ic_car_home.jpg`
- Image size: 200 × 200 pixels
- The source bytes are unchanged; no cropping, recolouring or enlargement is applied.
- Android encodes the decoded image as PNG when sending it to CarPlay, retaining
  the existing protocol image format and the original pixel dimensions.

The ORA name and logo belong to their respective owner. This local compatibility
build is independently modified and does not imply endorsement by ORA or GWM.
Upstream software copyright and licence notices remain in place.
