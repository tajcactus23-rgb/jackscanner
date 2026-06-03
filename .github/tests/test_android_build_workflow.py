"""
Tests for .github/workflows/android-build.yml

Validates the structure and content of the android-build workflow,
focusing on changes introduced in the PR:
- build-emulator-test job uses assembleFullDebug (not assembleDebug)
- Upload artifact path targets the full flavor APK specifically
- Workflow top-level name field is absent
"""

import os
import unittest

import yaml


WORKFLOW_PATH = os.path.join(
    os.path.dirname(__file__), "..", "workflows", "android-build.yml"
)


def load_workflow():
    with open(WORKFLOW_PATH, "r") as f:
        return yaml.safe_load(f)


class TestWorkflowTopLevel(unittest.TestCase):
    """Tests for top-level workflow structure."""

    def setUp(self):
        self.workflow = load_workflow()

    def test_workflow_file_is_valid_yaml(self):
        """Workflow file must be parseable as valid YAML."""
        self.assertIsNotNone(self.workflow)
        self.assertIsInstance(self.workflow, dict)

    def test_workflow_name_field_is_absent(self):
        """Top-level 'name' field should not be present (removed in PR)."""
        self.assertNotIn(
            "name",
            self.workflow,
            "The top-level 'name' field was removed in this PR and must not be present",
        )

    def test_workflow_has_on_trigger(self):
        """Workflow must define trigger events.

        Note: PyYAML (YAML 1.1) parses the bare word 'on' as boolean True,
        so the trigger block is keyed by True in the parsed dict.
        """
        # PyYAML 1.1 interprets bare 'on' as boolean True
        self.assertIn(True, self.workflow)

    def test_workflow_triggers_include_master(self):
        """Workflow must trigger on push to master branch."""
        # PyYAML 1.1 interprets bare 'on' as boolean True
        push_config = self.workflow[True]["push"]
        self.assertIn("master", push_config["branches"])

    def test_workflow_has_jobs(self):
        """Workflow must define at least one job."""
        self.assertIn("jobs", self.workflow)
        self.assertIsInstance(self.workflow["jobs"], dict)
        self.assertGreater(len(self.workflow["jobs"]), 0)


class TestBuildEmulatorTestJob(unittest.TestCase):
    """Tests for the build-emulator-test job (primary focus of PR changes)."""

    def setUp(self):
        workflow = load_workflow()
        self.job = workflow["jobs"]["build-emulator-test"]
        self.steps = self.job["steps"]

    def test_job_exists(self):
        """build-emulator-test job must exist in the workflow."""
        self.assertIsNotNone(self.job)

    def _find_step_by_name(self, name):
        for step in self.steps:
            if step.get("name") == name:
                return step
        return None

    def test_build_step_uses_assemble_full_debug(self):
        """Build Debug APK step must use assembleFullDebug task."""
        step = self._find_step_by_name("Build Debug APK")
        self.assertIsNotNone(step, "Step 'Build Debug APK' not found")
        self.assertIn(
            "assembleFullDebug",
            step["run"],
            "Build step must use 'assembleFullDebug' Gradle task",
        )

    def test_build_step_does_not_use_bare_assemble_debug(self):
        """Build step must NOT use assembleDebug (unflavored) task."""
        step = self._find_step_by_name("Build Debug APK")
        self.assertIsNotNone(step, "Step 'Build Debug APK' not found")
        run_command = step["run"]
        # assembleFullDebug contains 'assembleDebug' as substring, so check
        # that the bare 'assembleDebug' (i.e., the unflavored task) is absent.
        # The command should not have assembleDebug as a standalone token.
        import re
        bare_assemble_debug = re.search(r'\bassembleDebug\b', run_command)
        self.assertIsNone(
            bare_assemble_debug,
            f"Build step must not use bare 'assembleDebug' task; found: {run_command}",
        )

    def test_build_step_includes_stacktrace_flag(self):
        """Build step should include --stacktrace for debugging."""
        step = self._find_step_by_name("Build Debug APK")
        self.assertIsNotNone(step, "Step 'Build Debug APK' not found")
        self.assertIn("--stacktrace", step["run"])

    def test_upload_step_path_targets_full_flavor(self):
        """Upload APK step must target the full flavor APK path."""
        step = self._find_step_by_name("Upload APK")
        self.assertIsNotNone(step, "Step 'Upload APK' not found")
        artifact_path = step["with"]["path"]
        self.assertEqual(
            artifact_path,
            "jackscanner/app/build/outputs/apk/full/debug/app-full-debug.apk",
            "Upload path must point to the full-flavor debug APK",
        )

    def test_upload_step_path_contains_full_flavor_directory(self):
        """Upload APK path must include the 'full' flavor subdirectory."""
        step = self._find_step_by_name("Upload APK")
        self.assertIsNotNone(step, "Step 'Upload APK' not found")
        artifact_path = step["with"]["path"]
        self.assertIn(
            "/full/debug/",
            artifact_path,
            "APK path must contain the 'full/debug/' flavor directory",
        )

    def test_upload_step_path_is_not_wildcard(self):
        """Upload APK path must be a specific file, not a glob pattern."""
        step = self._find_step_by_name("Upload APK")
        self.assertIsNotNone(step, "Step 'Upload APK' not found")
        artifact_path = step["with"]["path"]
        self.assertNotIn(
            "*",
            str(artifact_path),
            "Upload path must be a specific file path, not a wildcard glob",
        )

    def test_upload_step_path_does_not_use_unflavored_debug_dir(self):
        """Upload path must not use the old unflavored apk/debug/ directory."""
        step = self._find_step_by_name("Upload APK")
        self.assertIsNotNone(step, "Step 'Upload APK' not found")
        artifact_path = step["with"]["path"]
        # Old path was apk/debug/*.apk — must not use apk/debug/ anymore
        self.assertNotIn(
            "apk/debug/",
            artifact_path,
            "Upload path must not reference the unflavored 'apk/debug/' directory",
        )

    def test_upload_step_artifact_name(self):
        """Upload APK step must use 'debug-apk' as artifact name."""
        step = self._find_step_by_name("Upload APK")
        self.assertIsNotNone(step, "Step 'Upload APK' not found")
        self.assertEqual(step["with"]["name"], "debug-apk")

    def test_upload_step_if_no_files_found_is_error(self):
        """Upload step must fail if the APK file is not found (regression guard)."""
        step = self._find_step_by_name("Upload APK")
        self.assertIsNotNone(step, "Step 'Upload APK' not found")
        self.assertEqual(
            step["with"].get("if-no-files-found"),
            "error",
            "if-no-files-found must be 'error' to catch missing APK builds",
        )

    def test_upload_step_uses_upload_artifact_v4(self):
        """Upload APK step must use actions/upload-artifact@v4."""
        step = self._find_step_by_name("Upload APK")
        self.assertIsNotNone(step, "Step 'Upload APK' not found")
        self.assertEqual(step.get("uses"), "actions/upload-artifact@v4")

    def test_build_command_targets_jackscanner_directory(self):
        """Build command must run from the jackscanner subdirectory."""
        step = self._find_step_by_name("Build Debug APK")
        self.assertIsNotNone(step, "Step 'Build Debug APK' not found")
        self.assertIn(
            "cd jackscanner",
            step["run"],
            "Build command must cd into jackscanner directory",
        )


class TestBuildEmulatorTestJobConsistency(unittest.TestCase):
    """Consistency tests ensuring the build task and artifact path align."""

    def setUp(self):
        workflow = load_workflow()
        self.job = workflow["jobs"]["build-emulator-test"]
        self.steps = self.job["steps"]

    def _find_step_by_name(self, name):
        for step in self.steps:
            if step.get("name") == name:
                return step
        return None

    def test_build_flavor_matches_upload_path_flavor(self):
        """The Gradle flavor in the build command must match the upload artifact path flavor."""
        build_step = self._find_step_by_name("Build Debug APK")
        upload_step = self._find_step_by_name("Upload APK")
        self.assertIsNotNone(build_step)
        self.assertIsNotNone(upload_step)

        # Build uses assembleFullDebug => flavor is 'full'
        build_command = build_step["run"]
        self.assertIn("assembleFullDebug", build_command)

        # Upload path must contain 'full' flavor directory
        artifact_path = upload_step["with"]["path"]
        self.assertIn(
            "full",
            artifact_path,
            "Upload artifact path flavor must match the 'full' Gradle build flavor",
        )

    def test_upload_filename_matches_full_flavor_convention(self):
        """Upload APK filename must follow the app-<flavor>-debug.apk naming convention."""
        upload_step = self._find_step_by_name("Upload APK")
        self.assertIsNotNone(upload_step)
        artifact_path = upload_step["with"]["path"]
        self.assertTrue(
            artifact_path.endswith("app-full-debug.apk"),
            f"APK filename must be 'app-full-debug.apk', got path: {artifact_path}",
        )


class TestOtherJobsUnchanged(unittest.TestCase):
    """Regression tests ensuring the other jobs were not accidentally modified."""

    def setUp(self):
        self.workflow = load_workflow()
        self.jobs = self.workflow["jobs"]

    def test_build_full_job_exists(self):
        """build-full job must still exist."""
        self.assertIn("build-full", self.jobs)

    def test_build_lite_job_exists(self):
        """build-lite job must still exist."""
        self.assertIn("build-lite", self.jobs)

    def test_release_job_exists(self):
        """release job must still exist."""
        self.assertIn("release", self.jobs)

    def test_build_full_uses_assemble_full_debug(self):
        """build-full job must use assembleFullDebug Gradle task."""
        steps = self.jobs["build-full"]["steps"]
        build_steps = [s for s in steps if "assembleFullDebug" in s.get("run", "")]
        self.assertGreater(
            len(build_steps),
            0,
            "build-full job must contain a step calling assembleFullDebug",
        )

    def test_build_lite_uses_assemble_lite_debug(self):
        """build-lite job must use assembleLiteDebug Gradle task."""
        steps = self.jobs["build-lite"]["steps"]
        build_steps = [s for s in steps if "assembleLiteDebug" in s.get("run", "")]
        self.assertGreater(
            len(build_steps),
            0,
            "build-lite job must contain a step calling assembleLiteDebug",
        )

    def test_release_needs_both_build_jobs(self):
        """release job must depend on both build-full and build-lite."""
        release_needs = self.jobs["release"].get("needs", [])
        self.assertIn("build-full", release_needs)
        self.assertIn("build-lite", release_needs)


if __name__ == "__main__":
    unittest.main()
