#!/bin/bash

hexo clean && hexo g && hexo s
rm -rf ~/Workspace/fun-code/oj-code/docs
cp -r public ~/Workspace/fun-code/oj-code/docs
cd ~/Workspace/fun-code/oj-code/ && git add . && git commit -am 'update blog' && git push origin master
cd ~/Documents/my-study/MyBlogs/oj/ && git add ./source/_posts -f && git commit -am 'update english blog' && git push origin master
