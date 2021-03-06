package a.versioninfo;
// VersionInfo.java
// Andrew Davison, December 2011, ad@fivedots.psu.ac.th
// Print OpenNI version.


import org.OpenNI.*;

public class VersionInfo
{
  public static void main(String args[])
  {
    try {
      Context context = new Context();
      Version vers = context.getVersion();
      System.out.println("OpenNI v." + 
            vers.getMajor() + "." + vers.getMinor() + "." +
            vers.getMaintenance() + "." + vers.getBuild()); 
      context.release();
    } 
    catch (GeneralException e) {
      System.out.println(e);
    }
  }  // end of main()

} // end of VersionInfo class

