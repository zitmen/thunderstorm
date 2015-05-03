## Links
  * [Documentation wiki](https://github.com/zitmen/thunderstorm/wiki)
  * [Discussion group](http://groups.google.com/group/thunderstorm-users)
  * [Issues](https://github.com/zitmen/thunderstorm/issues)

## News
  * The project has moved here, because [Google announced](http://google-opensource.blogspot.cz/2015/03/farewell-to-google-code.html) that Google Code service will be turned down this year.
  * Due to recent changes on Google Drive, previous versions of ThunderSTORM (<1.3) cannot be updated automatically using the build-in updater. This has been fixed but you might need to install the [latest stable version](https://googledrive.com/host/0BzOGc-xMFyDYR1JaelZYQmJsaUE/builds/stable/latest.html) manually.

## About ThunderSTORM
ThunderSTORM is an open-source, interactive, and modular plug-in for [ImageJ](http://rsb.info.nih.gov/ij/) designed for automated processing, analysis, and visualization of data acquired by single molecule localization microscopy methods such as PALM and STORM. Our philosophy in developing ThunderSTORM has been to offer an extensive collection of processing and post-processing methods so that users can easily adapt the process of analysis to their data.

## ThunderSTORM 1.3 released! (2014-11-08)
<a href="https://googledrive.com/host/0BzOGc-xMFyDYR1JaelZYQmJsaUE/builds/stable/latest.html">
<img src="https://googledrive.com/host/0BzOGc-xMFyDYR1JaelZYQmJsaUE/wiki/thunderstorm-logo-download.png" />
</a>

## Features
  * **Easy to use:** The default settings are designed to produce good results on many datasets. At the same time experienced users have a lot of freedom to configure the process of data analysis according to their needs.
  * **Platform-independent:** ThunderSTORM is written in Java and has been tested on Windows, Linux, and MacOS.
  * **Data processing:** Extensive collection of methods for approximate and sub-diffraction molecular localization. The threshold for detection of molecules can be specified using a mathematical expression.
  * **3D super-resolution imaging:**  Astigmatism approach with integrated calibration tool.
  * **Multiple-emitter fitting analysis:** Both 2D and 3D imaging using statistical model selection methods to determine the optimum number of molecules to fit.
  * **Post-processing methods:** Filtering molecules with poor localization or other user-defined criteria, local density based filtering, merging molecules reappearing in subsequent frames, removing duplicated molecules obtained in multiple emitter analysis, correction of molecular coordinates for lateral drift of the sample, support for multiple Z-stage data acquisition, coordinate based co-localization, exporting and visualizing localized molecules in user-defined region of interest, and possibility to undo the last change or to restore the original values.
  * **Live preview:** Instant live preview as the user modifies the post-processing criteria.
  * **Visualization methods:** 2D or slice-by-slice 3D visualization using scatter plot, histogram (with jittering option), average shifted histogram, Gaussian rendering.
  * **Import/export:** Input image sequences and the final super-resolution images can be opened or saved in any format supported by ImageJ or by any of its plug-ins. Localization results from previously processed datasets and ground-truth positions of the molecules can be imported or exported for further analysis in the following formats: CSV, XLS, XML, YAML, JSON, Google protocol buffer, and Tagged Spot File format.
  * **Simulation engine:** Tools for creation of realistic simulated data and for quantitative performance evaluation of localization algorithms using Monte-Carlo simulations.
  * **Parallel processing:** Processing of the input data is handled in parallel.
  * **Support for batch processing:** ThunderSTORM takes full advantage of ImageJ's Macro Language which makes batch processing possible (see the [BatchProcessing example]).
  * **Extendable:** The functionality of ThunderSTORM can be easily extended to [Developers add more features]. ThunderSTORM is also fully interoperable with other ImageJ plugins, thus the functionality can be indirectly extend by adding new plugins into ImageJ.


## Getting started
Install [ImageJ](http://imagej.nih.gov/ij/index.html) and download the latest version of [ThunderSTORM](https://googledrive.com/host/0BzOGc-xMFyDYR1JaelZYQmJsaUE/builds/stable/latest.html). For installation, copy the downloaded file into ImageJ's plugin subdirectory and run ImageJ. See the [Installation Installation guide] for more information. To get started using ThunderSTORM, see the [Tutorials Tutorials]. Example data are provided [here](https://googledrive.com/host/0BzOGc-xMFyDYR1JaelZYQmJsaUE/data/12%20+%20cyl%20lens.zip).


## How to cite ThunderSTORM
If you use ThunderSTORM to process your data, please, cite our [paper](http://dx.doi.org/10.1093/bioinformatics/btu202): M. Ovesný, P. Křížek, J. Borkovec, Z. Švindrych, G. M. Hagen. _ThunderSTORM: a comprehensive ImageJ plugin for PALM and STORM data analysis and super-resolution imaging._ Bioinformatics 30(16):2389-2390, 2014.
