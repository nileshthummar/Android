#!/bin/bash

PATTERN="Env"

echo "MATCHED SCRIPT GOT ARG-1: $1 ; TAG: $CIRCLE_TAG"

# Reading environment from CHANGELOG if it is missing in tag message
if [ -z "$1" ]; then
    INPUT_FILE="CHANGELOG.md"
	MATCHED_VAL=$(grep -im1 "${PATTERN}" ${INPUT_FILE})
else
	MATCHED_VAL="$1"
	MATCHED_VAL=$(grep -im1 "${PATTERN}" <<< "${MATCHED_VAL}")
fi

echo "MATCHED: ${MATCHED_VAL}"

# Remove spaces
MATCHED_VAL=${MATCHED_VAL// /}
echo "Removing ' ' gives ${MATCHED_VAL}"

# Remove ':' -will remove everything until last ':' is obtained
MATCHED_VAL=${MATCHED_VAL##*:}
echo "Removing ':' gives ${MATCHED_VAL}"

# Remove '=' -will remove everything until last '=' is obtained
MATCHED_VAL=${MATCHED_VAL##*=}
echo "Removing '=' gives ${MATCHED_VAL}"

if grep -qi "dev*" <<< "${MATCHED_VAL}"; then
    echo "Uploading variant DevRelease to crashlytics"
    ./gradlew crashlyticsUploadDistributionDevRelease
else
    echo "Uploading variant ProdRelease to crashlytics"
    ./gradlew crashlyticsUploadDistributionProdRelease
fi

# Done