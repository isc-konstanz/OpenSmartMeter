![emonmuc header](img/emonmuc-logo.png)

This document describes how to install the [emonmuc](https://github.com/isc-konstanz/emonmuc/) (**e**nergy **mon**itoring **m**ulty **u**tility **c**ommunication), an open-source protocoll driver project to enable the communication with a variety of metering or other devices, developed based on the [OpenMUC](https://www.openmuc.org/) project.


---------------

# 1 Install OpenSmartMeter

This short documentation will assume the generic **version 1.0.0** of the driver as a simplification.
To install the OSGi bundle, simply download the latest release tarball and move the bundle into the emonmuc frameworks *bundles* directory

~~~shell
wget --quiet --show-progress https://github.com/isc-konstanz/OpenSmartMeter/releases/download/v1.0.0/OpenSmartMeter-1.0.0.tar.gz
tar -xzf OpenSmartMeter-1.0.0.tar.gz
cd OpenSmartMeter*
mv ./libs/openmuc-driver-smartmeter-1.0.0.jar /opt/emonmuc/bundles/
~~~

Afterwards restart the framework, for the driver to be started

~~~
emonmuc restart
~~~


## 1.1 Device templates

Next, device template files are provided by this project, to ease up the configuration of some new hardware devices.  
Those can be found at *lib/device/smartmeter* and should be moved to the corresponding directory in the emonmuc root:

~~~shell
mv ./libs/device/smartmeter /opt/emonmuc/lib/device/
~~~


## 1.2 Prepare the Serial Port

To use any serial port with the emonmuc framework, the open-source project [jRxTx](https://github.com/openmuc/jrxtx) is used. This, as well as some additional steps to use the UART Pins of the Raspberry Pi Platform, need to be prepared.  
The [Serial Port preparation guide](https://github.com/isc-konstanz/emonmuc/blob/master/docs/LinuxWiringPi.md) needs to be followed to do so.


## 1.3 Finish

At last, don't forget to remove the released tarball to avoid cluttering of your system.

~~~
cd ..
rm -rf ./OpenSmartMeter*
~~~
