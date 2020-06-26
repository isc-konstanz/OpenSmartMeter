![emonmuc header](docs/img/emonmuc-logo.png)

This project implements a communication protocol as part of [emonmuc](https://github.com/isc-konstanz/emonmuc/) (**e**nergy **mon**itoring **m**ulti **u**tility **c**ommunication), based on the open-source project [OpenMUC](https://www.openmuc.org/), a software framework based on Java and OSGi, that simplifies the development of customized *monitoring, logging and control* systems.


----------

# OpenSmartMeter

This protocol driver implements the communication with metering devices speaking the Smart Message Language (SML) or as an IEC 62056-21 mode A-D master, registering one or several slaves such as gas, water, heat, or electricity meters.

The project is originally based on the [j62056](https://www.openmuc.org/iec-62056-21/) library and further depends on the [jSML](https://www.openmuc.org/sml/) project.  
j62056 is an LGPL version 2.1 licensed implementation of the IEC 62056-21 protocol (modes A, B, C, and D) and jSML implements the Smart Message Language (SML), licensed under the Mozilla Public License v2.0. Both libraries were developed by the German research institute Fraunhofer ISE. To get in contact with the projects developers, visit their homepage at [openmuc.org](https://www.openmuc.org/).


## 1 Installation

To setup this protocol driver, [emonmuc](https://github.com/isc-konstanz/emonmuc/) needs to be installed. To do so, a comprehensive guide is provided on the projects GitHub page.

With emonmuc being installed, the driver may be enabled

~~~
emonmuc install smartmeter
~~~

To disable the driver, use

~~~
emonmuc remove smartmeter
~~~

This shell command will automatically set up the driver. If there is the need to manually install the driver, a separate [installation guide](docs/LinuxInstall.md) may be followed.


## 1.1 Install RXTX 

RXTX is a Java native library providing serial and parallel communication for the Java virtual machine. It is a necessary dependency for many communication devices, using e.g. RS485.

To install, download the binaries via debian repository:

~~~
sudo apt-get install librxtx-java
~~~

If the serial port is not connected to the Raspberry Pi via e.g. an USB interface but the Raspberrys internal UART pins, the [Serial Port should be prepared](https://github.com/isc-konstanz/emonmuc/blob/master/docs/LinuxSerialPort.md) accordingly.


## 2 Guide

With the protocol driver being enabled, some first steps can be taken to learn about the features of this project.  
For this purpose, a [First Steps guide](docs/FirstSteps.md) was documented to be followed.


----------

# Contact

This project is maintained by:

![ISC logo](docs/img/isc-logo.png)

- **[ISC Konstanz](http://isc-konstanz.de/)** (International Solar Energy Research Center)
- **Adrian Minde**: adrian.minde@isc-konstanz.de
