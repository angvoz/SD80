#!/bin/sh

# note: this only works on a Linux-x86 host with Symbian OS SDKs, and Raptor + RVCT + GCCE installed

# This executable is used to test the TestSourceToAddressMapping tests and some uses of TestDwarfReader;
# update anything in those tests if you run this script!

TOP=`pwd`
SYMFILES=../../SymbolFiles

# build GCC-x86 version
make -C Debug clean && make -C Debug && \
cp "$TOP/Debug/SimpleCpp" "$SYMFILES/SimpleCpp_gcc_x86.exe" 

# build RVCT 2.2 version
rm -f $EPOCROOT/epoc32/release/armv5/udeb/SimpleCppSymbian.exe.sym &&
sbs -b group/bld.inf -c armv5.udeb.rvct2_2 clean && sbs -b group/bld.inf -c armv5.udeb.rvct2_2 && \
cp $EPOCROOT/epoc32/release/armv5/udeb/SimpleCppSymbian.exe.sym "$SYMFILES/SimpleCpp_rvct_22.sym"

# build RVCT 4.0 version
rm -f $EPOCROOT/epoc32/release/armv5/udeb/SimpleCppSymbian.exe.sym &&
sbs -b group/bld.inf -c armv5.udeb.rvct4_0 clean && sbs -b group/bld.inf -c armv5.udeb.rvct4_0 && \
cp $EPOCROOT/epoc32/release/armv5/udeb/SimpleCppSymbian.exe.sym "$SYMFILES/SimpleCpp_rvct_40.sym"

# build GCC-E version
rm -f $EPOCROOT/epoc32/release/armv5/udeb/SimpleCppSymbian.exe.sym
sbs -b group/bld.inf -c arm.v5.udeb.gcce4_3_2  clean && sbs -b group/bld.inf -c arm.v5.udeb.gcce4_3_2 && \
cp $EPOCROOT/epoc32/release/armv5/udeb/SimpleCppSymbian.exe.sym "$SYMFILES/SimpleCpp_gcce_432.sym"

