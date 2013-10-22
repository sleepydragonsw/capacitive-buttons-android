"""
MakePro.py
By: Denver Coneybeare <denver@sleepydragon.org>
Jan 03, 2012

Makes changes to the source code to convert the app to the "Pro" version
"""

from __future__ import print_function
from __future__ import unicode_literals

import io
import re
import sys
import tempfile

class FileFilter(object):
    def __init__(self, regex_pattern, group1_replacement):
        self.expr = re.compile(regex_pattern)
        self.group1_replacement = group1_replacement
        self.match_found = False
    def filter(self, line):
        match = self.expr.match(line)
        if match is not None:
            self.match_found = True
            (start, end) = match.span(1)
            prefix = line[:start]
            suffix = line[end:]
            line = "{}{}{}".format(prefix, self.group1_replacement, suffix)
        yield line
    def error_message(self):
        return "regular expression match not found in file: {}".format(self.expr.pattern)

class AndroidManifestFilter(FileFilter):
    def __init__(self):
        FileFilter.__init__(self, r"\s*package=\"org.sleepydragon.capbutnbrightness()\"", ".pro")

class PrefsXmlFilter(FileFilter):
    def __init__(self):
        FileFilter.__init__(self, r"\s*android:targetPackage=\"org.sleepydragon.capbutnbrightness()\"", ".pro")

class FixRImportFilter(FileFilter):
    def __init__(self):
        FileFilter.__init__(self, r"\s*import org.sleepydragon.capbutnbrightness().R;", ".pro")

class VersionStringFilter(FileFilter):
    def __init__(self):
        FileFilter.__init__(self, r"\s*<string name=\"app_version_display\">[^<]*()</string>", " Pro")

class AppNameStringFilter(FileFilter):
    def __init__(self):
        FileFilter.__init__(self, r"\s*<string name=\"app_name\">[^<]*()</string>", " Pro")

class AppTitleStringFilter(FileFilter):
    def __init__(self):
        FileFilter.__init__(self, r"\s*<string name=\"title_activity_main\">[^<]*()</string>", " Pro")

class ImportNewRFilter(FileFilter):
    LINE_PREFIX = "package org.sleepydragon.capbutnbrightness;"
    def __init__(self):
        self.match_found = False
    def filter(self, line):
        yield line
        if line.strip() == self.LINE_PREFIX:
            self.match_found = True
            linesep = line[len(line.rstrip()):]
            import_line = "import org.sleepydragon.capbutnbrightness.pro.R;{}".format(linesep)
            yield import_line
    def error_message():
        return "line not found: {}".format(self.LINE_PREFIX)

class UpgradeBannerLayoutXmlFilter(FileFilter):
    BEGIN_LINE = "<!-- Begin upgrade banner -->"
    END_LINE = "<!-- End upgrade banner -->"
    def __init__(self):
        self.begin_found = False
        self.end_found = False
        self.match_found = False
    def filter(self, line):
        if line.strip() == self.BEGIN_LINE:
            self.begin_found = True
        elif line.strip() == self.END_LINE:
            self.end_found = True
        self.match_found = self.begin_found and self.end_found
        if not self.begin_found and not self.end_found:
            yield line
        elif self.begin_found and self.end_found:
            yield line
    def error_message():
        if not self.begin_found:
            return "line not found: {}".format(self.BEGIN_LINE)
        elif not self.end_found:
            return "line not found: {}".format(self.END_LINE)

class UpgradeBannerJavaFilter(FileFilter):
    LINE1 = "final View btnUpgrade = this.findViewById(R.id.btnUpgrade);"
    LINE2 = "btnUpgrade.setOnClickListener(this);"
    LINE3 = "if (view.getId() == R.id.btnUpgrade) {"
    def __init__(self):
        self.line1_found = False
        self.line2_found = False
        self.line3_found = False
        self.match_found = False
    def filter(self, line):
        if line.strip() == self.LINE1:
            self.line1_found = True
        elif line.strip() == self.LINE2:
            self.line2_found = True
        elif line.strip() == self.LINE3:
            self.line3_found = True
            i = line.find(self.LINE3)
            newline = line[len(line.rstrip()):]
            yield line[:i] + "if (1 == 2) {" + newline
        else:
            yield line
        self.match_found = (self.line1_found and self.line2_found
            and self.line3_found)
    def error_message():
        missing_lines = []
        if not self.line1_found:
            missing_lines.append(self.LINE1)
        if not self.line2_found:
            missing_lines.append(self.LINE2)
        if not self.line3_found:
            missing_lines.append(self.LINE3)
        return "{} lines not found: {}".format(
            len(missing_lines),
            ", ".join('"{}"'.format(x) for x in missing_lines))

filters = [
    ("AndroidManifest.xml", AndroidManifestFilter()),
    ("res/layout/activity_main.xml", UpgradeBannerLayoutXmlFilter()),
    ("res/values/strings.xml", VersionStringFilter()),
    ("res/values/strings.xml", AppNameStringFilter()),
    ("res/values/strings.xml", AppTitleStringFilter()),
    ("res/xml/preferences.xml", PrefsXmlFilter()),
    ("src/org/sleepydragon/capbutnbrightness/AboutActivity.java", ImportNewRFilter()),
    ("src/org/sleepydragon/capbutnbrightness/ButtonBrightnessAppWidgetProvider.java", ImportNewRFilter()),
    ("src/org/sleepydragon/capbutnbrightness/CreditsActivity.java", ImportNewRFilter()),
    ("src/org/sleepydragon/capbutnbrightness/MainActivity.java", ImportNewRFilter()),
    ("src/org/sleepydragon/capbutnbrightness/MainActivity.java", UpgradeBannerJavaFilter()),
    ("src/org/sleepydragon/capbutnbrightness/SetBrightnessService.java", ImportNewRFilter()),
    ("src/org/sleepydragon/capbutnbrightness/SettingsActivity.java", ImportNewRFilter()),
    ("src/org/sleepydragon/capbutnbrightness/debug/DebugActivity.java", FixRImportFilter()),
    ("src/org/sleepydragon/capbutnbrightness/debug/DebugLinesGenerator.java", FixRImportFilter()),
]

for (path, filter) in filters:
    print("Making Pro: {}".format(path))
    with tempfile.TemporaryFile() as tf:
        with io.open(path, "rt", encoding="UTF-8", newline="") as f:
            for (linenum, line) in enumerate(f):
                lines_filtered = filter.filter(line)
                for line_filtered in lines_filtered:
                    line_filtered_encoded = line_filtered.encode("UTF-8")
                    tf.write(line_filtered_encoded)

        if not filter.match_found:
            msg = filter.error_message()
            print("ERROR: {}: {}".format(path, msg), file=sys.stderr)
            sys.exit(1)

        tf.seek(0)
        with open(path, "wb") as f:
            while True:
                buf = tf.read(1024)
                if not buf:
                    break
                f.write(buf)
