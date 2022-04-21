package edu.hitsz.Dao;

import java.util.List;

public interface RecordDao {
    //void findByRank(int rank);

    List<Record> getAllRecord();

    void addRecord(Record record);

    void deleteRecord(Record record);

    void closeRecord();
}
