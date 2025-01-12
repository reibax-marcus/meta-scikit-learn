#
#   Copyright (c) 2016 Intel Corporation. All rights reserved.
#   Copyright (c) 2019 Luxoft Sweden AB
#
#   SPDX-License-Identifier: MIT
#

DESCRIPTION = "OpenBLAS is an optimized BLAS library based on GotoBLAS2 1.13 BSD version."
SUMMARY = "OpenBLAS : An optimized BLAS library"
AUTHOR = "Alexander Leiva <norxander@gmail.com>"
HOMEPAGE = "http://www.openblas.net/"
SECTION = "libs"
LICENSE = "BSD-3-Clause"

DEPENDS = "make libgfortran patchelf-native"

LIC_FILES_CHKSUM = "file://LICENSE;md5=5adf4792c949a00013ce25d476a2abc0"

SRC_URI = "git://github.com/xianyi/OpenBLAS.git;protocol=https;branch=develop"

SRCREV = "fab746240cc7e95569fde23af8942f8bc97d6d40"

S = "${WORKDIR}/git"

def map_arch(a, d):
        import re
        if re.match('i.86$', a): return 'ATOM'
        elif re.match('x86_64$', a): return 'ATOM'
        elif re.match('aarch32$', a): return 'CORTEXA9'
        elif re.match('aarch64$', a): return 'ARMV8'
        elif re.match('arm$', a): return 'ARMV7'
        return a

def map_bits(a, d):
        import re
        if re.match('i.86$', a): return 32
        elif re.match('x86_64$', a): return 64
        elif re.match('aarch32$', a): return 32
        elif re.match('aarch64$', a): return 64
        elif re.match('arm$', a): return 32
        return 32

def map_extra_options(a, d):
        import re
        if re.match('arm$', a): return '-mfpu=neon-vfpv4 -mfloat-abi=hard'
        return ''

do_compile () {
        oe_runmake HOSTCC="${BUILD_CC}"                                         \
                                CC="${TARGET_PREFIX}gcc ${TOOLCHAIN_OPTIONS} ${@map_extra_options(d.getVar('TARGET_ARCH', True), d)}" \
                                PREFIX=${exec_prefix} \
                                CROSS=1 \
                                CROSS_SUFFIX=${HOST_PREFIX} \
                                NO_STATIC=1 NO_LAPACK=1 NO_LAPACKE=1 NO_CBLAS=1 NO_AFFINITY=1 USE_OPENMP=1 \
                                BINARY='${@map_bits(d.getVar('TARGET_ARCH', True), d)}' \
                                TARGET='${@map_arch(d.getVar('TARGET_ARCH', True), d)}'
}

do_install() {
        oe_runmake HOSTCC="${BUILD_CC}"                                         \
                                CC="${TARGET_PREFIX}gcc ${TOOLCHAIN_OPTIONS}" \
                                PREFIX=${exec_prefix} \
                                CROSS=1 \
                                CROSS_SUFFIX=${HOST_PREFIX} \
                                NO_STATIC=1 NO_LAPACK=1 NO_LAPACKE=1 NO_CBLAS=1 NO_AFFINITY=1 USE_OPENMP=1 \
                                BINARY='${@map_bits(d.getVar('TARGET_ARCH', True), d)}' \
                                TARGET='${@map_arch(d.getVar('TARGET_ARCH', True), d)}' \
                                DESTDIR=${D} \
                                install


        rm -rf ${D}${bindir}

        cd ${D}${libdir}
        cp -ar libopenblas*r*.so libblas.so.3
        patchelf --set-soname libblas.so.3 libblas.so.3
        ln -s libblas.so.3 libblas.so
}

FILES:${PN} = "${libdir}/lib*"
FILES:${PN}-dev = "${includedir} ${libdir}/lib${PN}.a ${libdir}/libblas.a ${libdir}/cmake ${libdir}/pkgconfig ${libdir}/libopenblas.so ${libdir}/libblas.so"

DEPENDS:remove:class-native = "libgfortran"
BBCLASSEXTEND = "native"
