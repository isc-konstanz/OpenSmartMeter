![emonmuc header](docs/img/emonmuc-logo.png)

This project implements a communication protocol as part of [emonmuc](https://github.com/isc-konstanz/emonmuc/) (**e**nergy **mon**itoring **m**ulti **u**tility **c**ommunication), based on the open-source project [OpenMUC](https://www.openmuc.org/), a software framework based on Java and OSGi, that simplifies the development of customized *monitoring, logging and control* systems.


----------

# OpenSmartMeter

*This section is a placeholder and will be filled with a project description.*


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

This shell command will set up the driver, as instructed in the [setup script](setup.sh).  
If there is the need to manually install the driver, a separate [installation guide](docs/LinuxInstall.md) may be followed.


## 1.1 Install RXTX 

RXTX is a Java native library providing serial and parallel communication for the Java virtual machine. It is a necessary dependency for many communication devices, using e.g. RS485.

To install, download the binaries via debian repository:

~~~
sudo apt-get install librxtx-java
~~~

If the serial port is not connected to the Raspberry Pi via e.g. an USB interface but the Raspberrys internal UART pins, the [Serial Port should be prepared](https://github.com/isc-konstanz/emonmuc/blob/master/docs/LinuxSerialPort.md) accordingly.


----------

# Contact

This project is maintained by:

![ISC logo](docs/img/isc-logo.png)

- **[ISC Konstanz](http://isc-konstanz.de/)** (International Solar Energy Research Center)
- **Adrian Minde**: adrian.minde@isc-konstanz.de
