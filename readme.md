Bar Chart Demo App
===========

This application demonstrates the data usage chart from the Android Open Source
Project (AOSP).  The original source for the chart can be downloaded from:

https://android.googlesource.com/platform/packages/apps/Settings/

The version of the source in AOSP is tightly coupled to the NetworkStats classes
that are private.  Additionally, the source code references many other hidden
methods and resources.

This version of the chart is decoupled from AOSP.  The primary chart is now
a bar chart.  The date sweeps were enhanced to show the dates in a "flag" at
the top of the sweeps.  The warning and limit horizontal sweeps were removed
and an average sweep was added.

To use the chart, create an instance of ChartData and then bind it to the chart
using the bindChartData() method.  Then set the visible range of the chart.
Optionally, a listener can be added to listen for changes in the date range
sweeps.


Legal Stuff
-----------

By downloading and/or using this software you agree to the following terms of use:

    Copyright © 2015 Republic Wireless

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this software except in compliance with the License.
    You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
