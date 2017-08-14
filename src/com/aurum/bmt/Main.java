/*
 * Copyright (C) 2017 Aurum
 *
 * J3DBmt is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * J3DBmt is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.aurum.bmt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.BufferedInputStream;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length != 3 && args.length != 4)
            return;
        
        switch(args[0]) {
            case "-e": {
                Bmd bmd = new Bmd();
                bmd.read(new File(args[1]));
                
                Bmt bmt = new Bmt();
                bmt.MAT3 = bmd.MAT3;
                bmt.TEX1 = bmd.TEX1;
                bmt.write(new File(args[2]));
            } break;
            case "-i": {
                Bmd bmd = new Bmd();
                bmd.read(new File(args[1]));
                
                Bmt bmt = new Bmt();
                bmt.read(new File(args[2]));
                
                bmd.MAT3 = bmt.MAT3;
                bmd.TEX1 = bmt.TEX1;
                bmd.write(new File(args[3]));
            } break;
        }
    }
    
    public static byte[] SVR1 = new byte[] {
        (byte) 0x53, (byte) 0x56, (byte) 0x52, (byte) 0x33,
        (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
        (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
        (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF
    };
    
    public static class Bmd {
        public static final long SIGNATURE = 0x4A334432626D6433L;
        public byte[] INF1, VTX1, EVP1, DRW1, JNT1, SHP1, MAT3, MDL3, TEX1;
        
        public Bmd() {}
        
        public void read(File f) throws IOException {
            try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(f)))) {
                long signature = in.readLong();
                if (signature != SIGNATURE)
                    throw new IllegalArgumentException("unexpected file signature: " + String.format("%016X", signature));
                int filesize = in.readInt();
                int sections = in.readInt();
                
                in.skipBytes(0x10);
                
                System.out.println(String.format("BMD FILE DATA (filesize: %.2f KB)", filesize / 1000f));
                
                for (int i = 0 ; i < sections ; i++) {
                    in.mark(0x8);
                    
                    int section = in.readInt();
                    int size = in.readInt();
                    
                    System.out.println(String.format("- %08X %08X", section, size));
                    
                    in.reset();
                    
                    switch(section) {
                        case 0x494E4631: INF1 = new byte[size]; in.read(INF1); break;
                        case 0x56545831: VTX1 = new byte[size]; in.read(VTX1); break;
                        case 0x45565031: EVP1 = new byte[size]; in.read(EVP1); break;
                        case 0x44525731: DRW1 = new byte[size]; in.read(DRW1); break;
                        case 0x4A4E5431: JNT1 = new byte[size]; in.read(JNT1); break;
                        case 0x53485031: SHP1 = new byte[size]; in.read(SHP1); break;
                        case 0x4D415433: MAT3 = new byte[size]; in.read(MAT3); break;
                        case 0x4D444C33: MDL3 = new byte[size]; in.read(MDL3); break;
                        case 0x54455831: TEX1 = new byte[size]; in.read(TEX1); break;
                    }
                }
            }
        }
        
        public void write(File f) throws IOException {
            int sections = 0;
            int inf1size = 0, vtx1size = 0;
            int evp1size = 0, drw1size = 0;
            int jnt1size = 0, shp1size = 0;
            int mat3size = 0, mdl3size = 0;
            int tex1size = 0;
            
            if (INF1 != null) { inf1size = INF1.length; sections++; }
            if (VTX1 != null) { vtx1size = VTX1.length; sections++; }
            if (EVP1 != null) { evp1size = EVP1.length; sections++; }
            if (DRW1 != null) { drw1size = DRW1.length; sections++; }
            if (JNT1 != null) { jnt1size = JNT1.length; sections++; }
            if (SHP1 != null) { shp1size = SHP1.length; sections++; }
            if (MAT3 != null) { mat3size = MAT3.length; sections++; }
            if (MDL3 != null) { mdl3size = MDL3.length; sections++; }
            if (TEX1 != null) { tex1size = TEX1.length; sections++; }
            
            int totalsize = 0x20 + inf1size + vtx1size + evp1size + drw1size
                    + jnt1size + shp1size + mat3size + mdl3size + tex1size;
            
            try (DataOutputStream out = new DataOutputStream(new FileOutputStream(f))) {
                out.writeLong(SIGNATURE);
                out.writeInt(totalsize);
                out.writeInt(sections);
                out.write(SVR1);
                if (INF1 != null) out.write(INF1);
                if (VTX1 != null) out.write(VTX1);
                if (EVP1 != null) out.write(EVP1);
                if (DRW1 != null) out.write(DRW1);
                if (JNT1 != null) out.write(JNT1);
                if (SHP1 != null) out.write(SHP1);
                if (MAT3 != null) out.write(MAT3);
                if (MDL3 != null) out.write(MDL3);
                if (TEX1 != null) out.write(TEX1);
                out.flush();
            }
        }
    }
    
    public static class Bmt {
        public static final long SIGNATURE = 0x4A334432626D7433L;
        public byte[] MAT3, TEX1;
        
        public Bmt() {}
        
        public void read(File f) throws IOException {
            try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(f)))) {
                long signature = in.readLong();
                if (signature != SIGNATURE)
                    throw new IllegalArgumentException("unexpected file signature: " + String.format("%016X", signature));
                int filesize = in.readInt();
                int sections = in.readInt();
                
                in.skipBytes(0x10);
                
                System.out.println(String.format("BMT FILE DATA (filesize: %.2f KB)", filesize / 1000f));
                
                for (int i = 0 ; i < sections ; i++) {
                    in.mark(0x8);
                    
                    int section = in.readInt();
                    int size = in.readInt();
                    
                    System.out.println(String.format("- %08X %08X", section, size));
                    
                    in.reset();
                    
                    switch(section) {
                        case 0x4D415433: MAT3 = new byte[size]; in.read(MAT3); break;
                        case 0x54455831: TEX1 = new byte[size]; in.read(TEX1); break;
                    }
                }
            }
        }
        
        public void write(File f) throws IOException {
            int sections = 0;
            int mat3size = 0, tex1size = 0;
            
            if (MAT3 != null) { mat3size = MAT3.length; sections++; }
            if (TEX1 != null) { tex1size = TEX1.length; sections++; }
            
            int totalsize = 0x20 + mat3size + tex1size;
            
            try (DataOutputStream out = new DataOutputStream(new FileOutputStream(f))) {
                out.writeLong(SIGNATURE);
                out.writeInt(totalsize);
                out.writeInt(sections);
                out.write(SVR1);
                if (MAT3 != null) out.write(MAT3);
                if (TEX1 != null) out.write(TEX1);
                out.flush();
            }
        }
    }
}