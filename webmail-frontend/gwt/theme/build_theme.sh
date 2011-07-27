rm -fr org
mkdir -p org/minig/theme/minig
cp -r minig/* org/minig/theme/minig
find org -name ".svn" -type d |xargs rm -fr
jar cf minig-theme.jar org && rm -fr org
