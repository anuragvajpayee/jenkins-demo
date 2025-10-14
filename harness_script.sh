#!/bin/bash
set -e

# ==============================================================================
# Harness Shell Script to Replicate Jenkins Shared Library Logic
# ==============================================================================
#
# This script emulates the behavior of the `demoScript.groovy` shared library
# from the jenkins-demo repository. It uses `yq` to process YAML files.
#
# It expects the following environment variables to be set:
#   - TEAM: The team name (e.g., 'frontend')
#   - SUITE: The test suite name (e.g., 'ui-tests')
#   - TEST: The test name (e.g., 'smoke-test')
#   - CUSTOM_GREETING: (Optional) A custom greeting message
#   - CUSTOM_ENVIRONMENT: (Optional) A custom environment

# --- Step 1: Validate Inputs and Set Defaults ---
echo "
🔄 Step 1: Validating inputs..."

if [ -z "$TEAM" ] || [ -z "$SUITE" ] || [ -z "$TEST" ]; then
    echo "❌ Error: TEAM, SUITE, and TEST environment variables are required." >&2
    exit 1
fi

# Check if yq is installed
if ! command -v yq &> /dev/null; then
    echo "❌ Error: 'yq' command is not found. Please install yq (v4+)." >&2
    exit 1
fi

echo "✅ Inputs validated:"
echo "  - Team: $TEAM"
echo "  - Suite: $SUITE"
echo "  - Test: $TEST"

# --- Step 2: Load and Prepare Configurations ---
echo "
📚 Step 2: Loading and preparing configurations..."

# Define file paths
GLOBAL_CONFIG_FILE="config/global.yml"
TEAM_CONFIG_FILE="teams/$TEAM/markup.yml"

# Create temporary files for merging
GLOBAL_SETTINGS_TMP=$(mktemp)
TEAM_INFO_TMP=$(mktemp)
SUITE_INFO_TMP=$(mktemp)
TEST_INFO_TMP=$(mktemp)
CUSTOM_PARAMS_TMP=$(mktemp)

# 1. Extract Global Settings
if [ -f "$GLOBAL_CONFIG_FILE" ]; then
    yq eval '.GLOBAL_SETTINGS' "$GLOBAL_CONFIG_FILE" > "$GLOBAL_SETTINGS_TMP"
    echo "  - Global config loaded."
else
    echo "  - ⚠️ Warning: Global config not found. Using empty defaults."
    echo '{}' > "$GLOBAL_SETTINGS_TMP"
fi

# 2. Extract Team, Suite, and Test Configs
if [ -f "$TEAM_CONFIG_FILE" ]; then
    # Extract team-level info (common settings, excluding suites)
    yq eval '.common | del(.suites)' "$TEAM_CONFIG_FILE" > "$TEAM_INFO_TMP"
    echo "  - Team config loaded for '$TEAM'."

    # Extract suite-level info
    yq eval ".common.suites.$SUITE | del(.tests)" "$TEAM_CONFIG_FILE" > "$SUITE_INFO_TMP" 2>/dev/null || echo '{}' > "$SUITE_INFO_TMP"
    echo "  - Suite config extracted for '$SUITE'."

    # Extract test-level info
    yq eval ".common.suites.$SUITE.tests.$TEST" "$TEAM_CONFIG_FILE" > "$TEST_INFO_TMP" 2>/dev/null || echo '{}' > "$TEST_INFO_TMP"
    echo "  - Test config extracted for '$TEST'."
else
    echo "  - ⚠️ Warning: Team config file not found for '$TEAM'. Using empty defaults."
    echo '{}' > "$TEAM_INFO_TMP"
    echo '{}' > "$SUITE_INFO_TMP"
    echo '{}' > "$TEST_INFO_TMP"
fi

# 3. Create Custom Parameters YAML
echo "  - Processing custom parameters..."
yaml_content="{}"
if [ -n "$CUSTOM_GREETING" ]; then
    yaml_content=$(yq eval ".greeting = \"$CUSTOM_GREETING\"" - <<< "$yaml_content")
fi
if [ -n "$CUSTOM_ENVIRONMENT" ]; then
    yaml_content=$(yq eval ".environment = \"$CUSTOM_ENVIRONMENT\"" - <<< "$yaml_content")
fi
echo "$yaml_content" > "$CUSTOM_PARAMS_TMP"

# --- Step 3: Merge Configurations ---
echo "
⚙️ Step 3: Merging configuration hierarchy..."

FINAL_CONFIG_TMP=$(mktemp)

# Merge all temp files. The last file in the list has the highest precedence.
yq eval-all 'reduce . as $item ({}; . * $item)' \
    "$GLOBAL_SETTINGS_TMP" \
    "$TEAM_INFO_TMP" \
    "$SUITE_INFO_TMP" \
    "$TEST_INFO_TMP" \
    "$CUSTOM_PARAMS_TMP" > "$FINAL_CONFIG_TMP"

echo "✅ Configuration hierarchy merged successfully."

# --- Step 4: Final Configuration Summary ---
echo "
📋 Step 4: Final Configuration Summary:"
yq eval '.' "$FINAL_CONFIG_TMP" | sed 's/^/  /g'

# --- Step 5: Execute Test (Emulated) ---
echo "
🧪 Step 5: Executing Test..."

GREETING=$(yq eval '.greeting' "$FINAL_CONFIG_TMP")
TEST_COMMAND=$(yq eval '.testCommand' "$FINAL_CONFIG_TMP")
ENVIRONMENT=$(yq eval '.environment' "$FINAL_CONFIG_TMP")

echo "  - Greeting: $GREETING"
echo "  - Running: $TEST_COMMAND"
echo "  - Environment: $ENVIRONMENT"
echo "✅ Test execution completed successfully!"

# --- Step 6: Generate Output ---
echo "
📊 Step 6: Generating JSON Output..."

# Add runtime info to the final config for the output JSON
JSON_OUTPUT=$(yq eval ".team = \"$TEAM\" | .suite = \"$SUITE\" | .test = \"$TEST\" | .status = \"SUCCESS\"" "$FINAL_CONFIG_TMP")

echo "$JSON_OUTPUT"

# Clean up temporary files
rm "$GLOBAL_SETTINGS_TMP" "$TEAM_INFO_TMP" "$SUITE_INFO_TMP" "$TEST_INFO_TMP" "$CUSTOM_PARAMS_TMP" "$FINAL_CONFIG_TMP"

echo "
🎉 DEMO SCRIPT EXECUTION COMPLETED SUCCESSFULLY!"
