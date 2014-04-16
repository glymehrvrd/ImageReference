#!/usr/bin/env python

import urllib2
import re
import os.path
import sys

# download from dangdang
url_link = 'http://category.dangdang.com/all/?category_path=01.01.08.00.00.00'
reimg = re.compile('<a title="(.{1,100})"   class="pic"  href="(.+?)"  target="_blank" ><img src=\'(.+?)\' alt=\'(.+?)\' />')

fd = urllib2.urlopen(url_link)
data = fd.read()
data = data.decode('gbk')
data = data.replace('\n','')
data = data.replace('\r','')
data = data.replace(' ',' ')

imgs = reimg.finditer(data)

with open('data.txt','w') as f:
    f.write(data.encode('utf8'))

i = 0
flist = open('filelist.csv','w')
for img in imgs:
    title = img.group(1)
    href = img.group(2)
    imgurl = img.group(3)
    imgurl = imgurl[0:-5] + 'w' + imgurl[-4:]    # change to download large picture

    flist.write(str(i)+'\n'+title.strip().encode('utf8')+'\n'+href.encode('utf8')+'\n')

    # fimg = urllib2.urlopen(imgurl)
    # ext = os.path.splitext(imgurl)[1]
    # with open(str(i) + ext, 'wb') as f:
    #     f.write(fimg.read())
    # fimg.close()

    print 'finish %d' % i
    i += 1

flist.close()
