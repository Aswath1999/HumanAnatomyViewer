package anatomy;

import java.util.Collection;

public record ANode(String conceptId, String representationId, String name, Collection <ANode> children, Collection <String> fileIds ){
    public String toString(){
        return name + "("+conceptId +")";
    }
}

