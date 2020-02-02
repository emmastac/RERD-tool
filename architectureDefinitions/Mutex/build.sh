#!/bin/bash
FDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

set -e
set -x verbose

mkdir -p output build

bipc.sh -I . -p MutexOperands -d "MutexOperands()" \
 --gencpp-output output \
--gencpp-enable-marshalling \
--gencpp-follow-used-packages 

cd build

cmake $(ls -d ${FDIR}/output)
make

echo "Finished. Run: ./build/system"
