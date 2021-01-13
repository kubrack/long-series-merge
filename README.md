## Description

Time series are stored in files with the following format:
- files are multiline plain text files in ASCII encoding
- each line contains exactly one record
- each record contains date and integer value; records are encoded like so: YYYY-MM-DD:X
- dates within single file are non-duplicate and sorted in ascending order
- files can be bigger than RAM available on target host
- solution should be implemented in Clojure

The implementation accepts names of files as arguments, which merges arbitrary number of input files and writes result to STDOUT. 
The output follows the same format conventions as described above. 
Records with the same date value will be merged into one by summing up "X" values.

## Usage

    $ java -jar app-1.0-standalone.jar file1 [file2 ...] > outfile

## License

Copyright © 2020 kubrack@gmail.com

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
