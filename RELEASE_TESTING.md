# Release Testing Guide

This guide explains how to test the release workflow and create a learning/test release.

## Overview

The release workflow has been configured to create **draft releases** by default. This allows you to:
- Review the built APK before publishing
- Edit release notes if needed
- Test the release process safely

## Creating a Test Release

### Step 1: Create and Push a Tag

To trigger the release workflow, create a new version tag and push it:

```bash
# Create a new tag (use appropriate version number)
git tag v.1.0.1

# Push the tag to trigger the workflow
git push origin v.1.0.1
```

**Tag naming convention:** Use `v.X.Y.Z` format (e.g., `v.1.0.1`, `v.1.1.0`, `v.2.0.0`)

### Step 2: Monitor the Workflow

1. Go to the **Actions** tab in GitHub: https://github.com/vibecoding-inc/vibestempel/actions
2. Look for the "Create Release with APK" workflow run
3. Click on the workflow run to see its progress
4. Wait for all steps to complete (usually 2-3 minutes)

The workflow will:
- Set up JDK 17
- Build the release APK
- Create a draft release
- Upload the APK as a release asset

### Step 3: Review the Draft Release

Once the workflow completes successfully:

1. Go to the **Releases** page: https://github.com/vibecoding-inc/vibestempel/releases
2. You'll see your new release marked as **Draft**
3. Click on the draft release to review it

### Step 4: Edit (Optional)

While the release is in draft mode, you can:
- Edit the release title
- Modify the release description
- Add additional notes or warnings
- Upload additional files if needed

### Step 5: Publish or Delete

When you're satisfied with the release:

**To publish:**
- Click the **"Publish release"** button
- The release will become publicly visible

**To delete (for test releases):**
- Click **"Delete"** to remove the test release
- You can also delete the tag if needed:
  ```bash
  git tag -d v.1.0.1
  git push origin :refs/tags/v.1.0.1
  ```

## Example: Creating a Learning Release

Here's a complete example for creating a test release:

```bash
# Ensure you're on the latest commit
git pull origin main

# Create a test tag
git tag v.1.0.1

# Push the tag to trigger the workflow
git push origin v.1.0.1

# Now go to GitHub Actions to monitor the build
# After it completes, check the Releases page to see your draft release
```

## Troubleshooting

### Workflow doesn't start
- Verify the tag was pushed successfully: `git ls-remote --tags origin`
- Check that the tag follows the `v*` pattern
- Ensure GitHub Actions is enabled for the repository

### Build fails
- Check the workflow logs for specific errors
- Verify that `SUPABASE_URL` and `SUPABASE_KEY` secrets are configured
- Ensure the JDK and Android build tools are compatible

### APK not uploaded
- Check if the build step completed successfully
- Verify the APK was created at `app/build/outputs/apk/release/app-release-unsigned.apk`
- Review the "Create GitHub Release" step logs

## Notes

- Draft releases are **not visible to the public** until published
- Each tag can only be used once - delete the tag before reusing the same version number
- The APK is unsigned and intended for development/testing purposes only
- Production releases should use a signed APK with a proper keystore

## Further Reading

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Creating Releases](https://docs.github.com/en/repositories/releasing-projects-on-github/managing-releases-in-a-repository)
- [Android APK Signing](https://developer.android.com/studio/publish/app-signing)
