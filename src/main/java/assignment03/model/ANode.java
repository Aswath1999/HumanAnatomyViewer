package assignment03.model;


import java.util.Collection;

public record ANode(String conceptId, String representationId, String name, Collection <assignment03.model.ANode> children, Collection <String> fileIds ){
    public String toString(){
        return name + "("+conceptId +")";
    }
}