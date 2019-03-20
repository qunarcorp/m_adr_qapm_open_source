/*
 * Copyright (c) 2012-2013 NetEase, Inc. and other contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package com.mqunar.qapm.tracing;

import android.content.Context;
import android.os.Process;
import android.util.Log;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * operate CPU information
 */
public class CpuInfo {

    private static final String LOG_TAG = "CpuTracer";

    private Context context;
    private long processCpu;
    private ArrayList<Long> idleCpu = new ArrayList<>();
    private ArrayList<Long> totalCpu = new ArrayList<>();
    private boolean isInitialStatics = true;
    private ArrayList<String> cpuUsedRatio;
    private ArrayList<Long> totalCpu2 = new ArrayList<>();
    private long processCpu2;
    private ArrayList<Long> idleCpu2 = new ArrayList<>();
    private String processCpuRatio = "";
    private ArrayList<String> totalCpuRatio = new ArrayList<>();
    private int pid;

    private static final String INTEL_CPU_NAME = "model name";
    private static final String CPU_DIR_PATH = "/sys/devices/system/cpu/";
    private static final String CPU_INFO_PATH = "/proc/cpuinfo";
    private static final String CPU_STAT = "/proc/stat";

    public CpuInfo(Context context) {
        this.pid = Process.myPid();
        this.context = context;
        cpuUsedRatio = new ArrayList<>();
    }

    /**
     * read the status of CPU.
     */
    public void readCpuStat() {
        String processPid = Integer.toString(pid);
        String cpuStatPath = "/proc/" + processPid + "/stat";
        try {
            // monitor cpu stat of certain process
            RandomAccessFile processCpuInfo = new RandomAccessFile(cpuStatPath, "r");
            String line = "";
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.setLength(0);
            while ((line = processCpuInfo.readLine()) != null) {
                stringBuffer.append(line + "\n");
            }
            String[] tok = stringBuffer.toString().split(" ");
            processCpu = Long.parseLong(tok[13]) + Long.parseLong(tok[14]);
            processCpuInfo.close();
        } catch (FileNotFoundException e) {
            Log.w(LOG_TAG, "FileNotFoundException: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
        readTotalCpuStat();
    }

    /**
     * read stat of each CPU cores
     */
    private void readTotalCpuStat() {
        try {
            // monitor total and idle cpu stat of certain process
            RandomAccessFile cpuInfo = new RandomAccessFile(CPU_STAT, "r");
            String line = "";
            while ((null != (line = cpuInfo.readLine())) && line.startsWith("cpu")) {
                String[] toks = line.split("\\s+");
                idleCpu.add(Long.parseLong(toks[4]));
                totalCpu.add(Long.parseLong(toks[1]) + Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[4])
                        + Long.parseLong(toks[6]) + Long.parseLong(toks[5]) + Long.parseLong(toks[7]));
            }
            cpuInfo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * get CPU name.
     *
     * @return CPU name
     */
    public String getCpuName() {
        try {
            RandomAccessFile cpuStat = new RandomAccessFile(CPU_INFO_PATH, "r");
            // check cpu type
            String line;
            while (null != (line = cpuStat.readLine())) {
                String[] values = line.split(":");
                if (values[0].contains(INTEL_CPU_NAME) || values[0].contains("Processor")) {
                    cpuStat.close();
                    Log.d(LOG_TAG, "CPU name=" + values[1]);
                    return values[1];
                }
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException: " + e.getMessage());
        }
        return "";
    }

    /**
     * display directories naming with "cpu*"
     *
     * @author andrewleo
     */
    class CpuFilter implements FileFilter {
        @Override
        public boolean accept(File pathname) {
            // Check if filename matchs "cpu[0-9]"
            if (Pattern.matches("cpu[0-9]", pathname.getName())) {
                return true;
            }
            return false;
        }
    }

    /**
     * get CPU core numbers
     *
     * @return cpu core numbers
     */
    public int getCpuNum() {
        try {
            // Get directory containing CPU info
            File dir = new File(CPU_DIR_PATH);
            // Filter to only list the devices we care about
            File[] files = dir.listFiles(new CpuFilter());
            return files.length;
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
    }

    /**
     * get CPU core list
     *
     * @return cpu core list
     */
    public ArrayList<String> getCpuList() {
        ArrayList<String> cpuList = new ArrayList<>();
        try {
            // Get directory containing CPU info
            File dir = new File(CPU_DIR_PATH);
            // Filter to only list the devices we care about
            File[] files = dir.listFiles(new CpuFilter());
            for (int i = 0; i < files.length; i++) {
                cpuList.add(files[i].getName());
            }
            return cpuList;
        } catch (Exception e) {
            e.printStackTrace();
            cpuList.add("cpu0");
            return cpuList;
        }
    }

    /**
     * reserve used ratio of process CPU and total CPU, meanwhile collect
     * network traffic.
     *
     * @return network traffic ,used ratio of process CPU and total CPU in
     * certain interval
     */
    public ArrayList<String> getCpuRatioInfo() {

        DecimalFormat fomart = new DecimalFormat();
        fomart.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
        fomart.setGroupingUsed(false);
        fomart.setMaximumFractionDigits(2);
        fomart.setMinimumFractionDigits(2);

        cpuUsedRatio.clear();
        idleCpu.clear();
        totalCpu.clear();
        totalCpuRatio.clear();
        readCpuStat();

        if (isInitialStatics) {
            isInitialStatics = false;
        } else {

            StringBuffer totalCpuBuffer = new StringBuffer();
            if (null != totalCpu2 && totalCpu2.size() > 0) {
                processCpuRatio = fomart
                        .format(100 * ((double) (processCpu - processCpu2)
                                / ((double) (totalCpu.get(0) - totalCpu2.get(0)))));
                for (int i = 0; i < (totalCpu.size() > totalCpu2.size() ? totalCpu2.size() :
                        totalCpu.size()); i++) {
                    String cpuRatio = "0.00";
                    if (totalCpu.get(i) - totalCpu2.get(i) > 0) {
                        cpuRatio = fomart.
                                format(100 * ((double) ((totalCpu.get(i) - idleCpu.get(i)) - (totalCpu2.get(i) - idleCpu2.get(i)))
                                        / (double) (totalCpu.get(i) - totalCpu2.get(i))));
                    }
                    totalCpuRatio.add(cpuRatio);
                    totalCpuBuffer.append(cpuRatio + ",");
                }
            } else {
                processCpuRatio = "0";
                totalCpuRatio.add("0");
                totalCpuBuffer.append("0,");
                totalCpu2 = (ArrayList<Long>) totalCpu.clone();
                processCpu2 = processCpu;
                idleCpu2 = (ArrayList<Long>) idleCpu.clone();
            }

            if (isPositive(processCpuRatio) && isPositive(totalCpuRatio.get(0))) {
                totalCpu2 = (ArrayList<Long>) totalCpu.clone();
                processCpu2 = processCpu;
                idleCpu2 = (ArrayList<Long>) idleCpu.clone();
                cpuUsedRatio.add(processCpuRatio);
                cpuUsedRatio.add(totalCpuRatio.get(0));
            }
        }
        return cpuUsedRatio;
    }

    /**
     * is text a positive number
     *
     * @param text
     *
     * @return
     */
    private boolean isPositive(String text) {
        Double num;
        try {
            num = Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return false;
        }
        return num >= 0;
    }


    public float readUsage() {
        try {
            RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r");
            String load = reader.readLine();

            String[] toks = load.split(" +");  // Split on one or more spaces

            long idle1 = Long.parseLong(toks[4]);
            long cpu1 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[5])
                    + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

            try {
                Thread.sleep(360);
            } catch (Exception e) {
            }

            reader.seek(0);
            load = reader.readLine();
            reader.close();

            toks = load.split(" +");

            long idle2 = Long.parseLong(toks[4]);
            long cpu2 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[5])
                    + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

            return (float) (cpu2 - cpu1) / ((cpu2 + idle2) - (cpu1 + idle1));

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return 0;
    }

}
