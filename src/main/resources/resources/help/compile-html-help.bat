@echo on

echo %1

set CWD=%CD%

C:
chdir C:\cygwin64\bin
bash --login -i -c "cd '%CWD%'; ./compile-html-help.sh "%1""
