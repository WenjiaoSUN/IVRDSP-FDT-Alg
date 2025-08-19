package InstanceGet;

import InstanceInput.Input;

public class City {
    private int idOfCity;
    private String nameOfCity;

    private CoordinateOfCity coordinateOfCity;


    public City( int idOfCity, String nameOfCity, CoordinateOfCity coordinateOfCity) {
        this.idOfCity = idOfCity;
        this.nameOfCity = nameOfCity;
        this.coordinateOfCity = coordinateOfCity;
            }

    public int getIdOfCity() {
        return idOfCity;
    }

    public void setIdOfCity(int idOfCity) {
        this.idOfCity = idOfCity;
    }

    public CoordinateOfCity getCoordinate() {
        return coordinateOfCity;
    }

    public String getNameOfCity() {
        return nameOfCity;
    }









    @Override
    public String toString() {
        return
                 idOfCity+" "+nameOfCity+" "+ coordinateOfCity.getX() +" " +coordinateOfCity.getY();
    }

    public static void main(String[] args) {

        CoordinateOfCity coordinateOfCity1 = new CoordinateOfCity(3,4);
        City city = new City(0, Character.toString((char) 65), coordinateOfCity1); //transform the number to corresponding A to Z;
        System.out.println(city);

    }
}
