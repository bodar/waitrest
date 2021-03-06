package com.googlecode.waitrest;

import com.googlecode.totallylazy.Mapper;

public class LocationInFile {
    private String filePath;
    private long position;

    public LocationInFile(String filePath, long position) {
        this.filePath = filePath;
        this.position = position;
    }

    public static Mapper<LocationInFile, String> filePath() {
        return new Mapper<LocationInFile, String>() {
            @Override
            public String call(LocationInFile locationInFile) throws Exception {
                return locationInFile.getFilePath();
            }
        };
    }

    public String getFilePath() {
        return filePath;
    }

    public long getPosition() {
        return position;
    }

    @Override
    public String toString(){
        return String.format("%s:%d", filePath, position);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LocationInFile that = (LocationInFile) o;

        return position == that.position && filePath.equals(that.filePath);
    }

    @Override
    public int hashCode() {
        int result = filePath.hashCode();
        result = 31 * result + (int) (position ^ (position >>> 32));
        return result;
    }
}
