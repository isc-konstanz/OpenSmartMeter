![emonmuc header](img/emonmuc-logo.png)

This document describes how to install the  (**e**nergy **mon**itoring **m**ulty **u**tility **c**ommunication), an open-source protocoll driver project to enable the communication with a variety of metering or other devices, developed based on the [OpenMUC](https://www.openmuc.org/) project.


---------------

# 1 Install OpenIEC62056-21

This short documentation will assume the generic **version 1.0.0** of the driver as a simplification.
To install the OSGi bundle, simply download the latest release tarball and move the bundle into the emonmuc frameworks *bundles* directory

~~~shell
wget --quiet --show-progress https://github.com/isc-konstanz/OpenIEC62056-21/releases/download/v1.0.0/OpenIEC62056-21-1.0.0.tar.gz
tar -xzf OpenIEC62056-21-1.0.0.tar.gz
mv ./OpenIEC62056-21/lib/openmuc-driver-iec62056-21-1.0.0.jar /opt/emonmuc/bundles/
~~~

Afterwards restart the framework, for the driver to be started

~~~
emonmuc restart
~~~


## 1.1 Device templates

Next, device template files are provided by this project, to ease up the configuration of some new hardware devices.  
Those can be found at *lib/device/iec62056-21* and should be moved to the corresponding directory in the emonmuc root:

~~~shell
mv ./OpenIEC62056-21/lib/device/iec62056-21 /opt/emonmuc/lib/device/
~~~


## 1.2 Finish

At last, don't forget to remove the released tarball to avoid cluttering of your system.

~~~
rm -rf ./OpenIEC62056-21*
~~~
