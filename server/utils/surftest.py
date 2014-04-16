#!/usr/bin/env python
import os

prog = "/home/glyme/myws/feature-match/Debug/feature-match"
src_img = "/home/glyme/Zend/workspaces/DefaultWorkspace/image-detect/imgtmp.jpg"

sim = 0;
match_file = ""
for root, dirs, files in os.walk("/home/glyme/Zend/workspaces/DefaultWorkspace/image-detect/picstore/"):
    for f in files:
        print "------------------------"
        dist_img = os.path.join(root, f)
        os.system("%s %s %s" % (prog, src_img, dist_img))
        print dist_img
