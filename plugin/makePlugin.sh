cd ../bin
rm -f ../plugin/particles/library/particles.jar
find simong -wholename '*\.svn' -prune -o -print | zip ../plugin/particles/library/particles.jar -@