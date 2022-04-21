package edu.hitsz.Dao;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RecordDaoImpl implements RecordDao{
    private final List<Record> records = new ArrayList<>();

    public RecordDaoImpl(){
        try {
            File file = new File("record.ser");
            if(file.exists()){
                ObjectInputStream out = new ObjectInputStream(
                        new FileInputStream(file));
                Record[] obj = (Record[]) out.readObject();
                List<Record> listObject = Arrays.asList(obj);
                records.addAll(listObject);
                out.close();
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void closeRecord(){
        try {
            File file = new File("record.ser");
            if(!file.exists()){
                file.createNewFile();
            }
            ObjectOutputStream oos = new ObjectOutputStream(
                    new FileOutputStream(file));
            Record[] tempRecords = new Record[records.size()];
            records.toArray(tempRecords);
            oos.writeObject(tempRecords);
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Record> getAllRecord(){
        return records;
    }

    @Override
    public void addRecord(Record record){
        boolean insertDone = false;
        if(records.isEmpty()){
            record.setRank(1);
            records.add(record);
            return;
        }
        for(Record recordi:records){
            if(record.getScore() >= recordi.getScore() && !insertDone){
                record.setRank(recordi.getRank());
                records.add(records.indexOf(recordi),record);
                insertDone = true;
                break;
            }
        }
        if (!insertDone){
            records.add(record);
        }
        else {
            for (int i = record.getRank();i<records.size();i++){
                records.get(i).setRank(records.get(i).getRank()+1);
            }
        }
    }

    @Override
    public void deleteRecord(Record record){
        records.remove(record);
        for(Record recordi:records){
            int index = records.indexOf(recordi);
            if (recordi.getRank() != index+1){
                recordi.setRank(index+1);
            }
        }
    }
}
