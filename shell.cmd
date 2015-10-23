@echo off
:: This script sets environment variables for Java and PostgreSQL
:: for COMP3220, and spawns a subshell with these variables set.
:: it makes assumptions about directory paths that only apply to those systems.

if "%Processor_Architecture%" == "x86" set ProgramFiles(x86)=%ProgramFiles%
path C:\Program Files\Java\jre1.8.0_31\bin;D:\Program Files (x86)\PostgreSQL\8.3\bin;%path%
set CLASSPATH=%CLASSPATH%;D:\Program Files\sqljdbc_4.1\enu\sqljdbc4.jar;E:\COMP3220_Project\postgresql-8.3-606.jdbc4.jar
start "COMP3220 Shell"
