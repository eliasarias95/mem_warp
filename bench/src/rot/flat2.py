import sys

from java.nio import *
from java.lang import *
from javax.swing import *

from edu.mines.jtk.dsp import Sampling
from edu.mines.jtk.io import *
from edu.mines.jtk.util.ArrayMath import *

from dnp import Flattener2
from dnp import LocalSlopeFinder
from util import *
from slopes import DynamicWarpingSlopes

seismicDir = System.getProperty("user.home")+"/home/git/mem_warp/bench/data/"

n1,n2 = 1800,601
#d1,d2 = 0.0006,0.005
#f1,f2 = 0.0,-1.5
#s1,s2 = Sampling(n1,d1,f1),Sampling(n2,d2,f2)
s1,s2 = Sampling(n1),Sampling(n2)

pmax = 5.0
T = True
F = False
paint = F
fw = 0.8
fh = 0.8

fxfile = "eliasData12"
gtfile = "_flattened"
pfile = "_p"
elfile = "_el"

def main(args):
  goSlopeSDW()
  goFlatten("sdw")

def goSlopeSDW():
  f = readImage(fxfile)
  p = copy(f)
  k  = 5
  r1 = 0.1
  r2 = 0.3
  h1 = 50.0
  h2 = 10.0
  ss1 = Sampling(n1);
  ss2 = Sampling(n2);
  sdw = DynamicWarpingSlopes(k,pmax,h1,h2,r1,r2,ss1,ss2)
  sdw.findSmoothSlopes(ss1,f,p)
  el = zerofloat(n1,n2)
  fill(1.0,el)
  writeImage("sdw"+pfile,p)
  writeImage("sdw"+elfile,el)
  cm = 5.0
  plot1(p,cm,"sdw slope",T)

def goFlatten(method):
  f = readImage(fxfile)
  p = readImage(method+pfile)
  el = readImage(method+elfile)
  #p = mul(d1/d2,p)
  el = pow(el,2.0)
  fl = Flattener2()
  fl.setIterations(0.01,100)
  fm = fl.getMappingsFromSlopes(s1,s2,p,el)
  gt = fm.flatten(f)
  writeImage(method+gtfile,gt)
  gt = readImage(method+gtfile)
  cm = 1.5e-5
  plot1(f,cm,"lsf_before_flattening",F)
  plot1(gt,cm,"lsf_after_flattening",F)

#############################################################################
# read/write files

def readImage(name):
  bo = ByteOrder.LITTLE_ENDIAN
  fileName = seismicDir+name+".rsf@"
  image = zerofloat(n1,n2)
  ais = ArrayInputStream(fileName,bo)
  ais.readFloats(image)
  ais.close()
  return image

def writeImage(name,image):
  bo = ByteOrder.LITTLE_ENDIAN
  fileName = seismicDir+name+".rsf@"
  aos = ArrayOutputStream(fileName,bo)
  aos.writeFloats(image)
  aos.close()
  return image

#############################################################################
# graphics

def plot1(f,cm,ttl,color):
  cbl = "Amplitude" #colorbar label
  #clip,interp,ttl,paint,color,slide,one
  Plot.plot(s1,s2,f,ttl,cbl,-cm,cm,color,paint);

def plot2(f,p,cm,ttl,color):
  cbl = "slope (samples/trace)" #colorbar label
  #clip, title, paint, slide, no. columns
  Plot.plot(s1,s2,f,ttl,cbl,-cm,cm,color,paint);

#############################################################################
# Run the function main on the Swing thread
import sys
class _RunMain(Runnable):
  def __init__(self,main):
    self.main = main
  def run(self):
    self.main(sys.argv)
def run(main):
  SwingUtilities.invokeLater(_RunMain(main)) 
run(main)
