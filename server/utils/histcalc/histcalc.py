#!/usr/bin/env python

import subprocess
import json
import os

prog = '../../feature-match'

def getHist(pic1, pic2):
    '''
    get histogram correlation of pic1 and pic2
    '''
    p = subprocess.Popen([prog, pic1, pic2], stdout=subprocess.PIPE)
    hist = float(p.stdout.read())
    return hist

if __name__ == '__main__':
    with open('conf.json') as f:
        s = f.read()
    conf = json.loads(s)
    ori_pic_dir = conf['ori_pic_dir']
    match_pic_dir = conf['match_pic_dir']
    pairs = conf['pairs']

    filelist = os.listdir(match_pic_dir)
    for p in pairs:
        original_pic = p['original_pic']
        match_pic = p['match_pic']

        highest = 0
        highest_pic = ""
        for f in filelist:
            hist = getHist(match_pic_dir + f, ori_pic_dir + original_pic)
            print hist
            if(hist > highest):
                highest_pic = f
                highest = hist
            if(f == match_pic):
                match_pic_score = hist
        print '''Sample picture %s: highest picture %s, score: %s\nexpected picture %s, socre: %s\n''' % (original_pic, highest_pic, highest, match_pic, match_pic_score)
