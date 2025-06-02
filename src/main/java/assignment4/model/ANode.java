package assignment4.model;

import java.util.Collection;
import java.util.Objects;

// class to create ANode objects
public record ANode(String conceptId, String representationId, String name, Collection<ANode> children, Collection<String> fileIds) {
    @Override
    public String toString() {
        return name + " (" + conceptId + ")";
    }

    @Override
    // method to check if two ANode objects are equal
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ANode other)) return false;
        return Objects.equals(conceptId, other.conceptId) && Objects.equals(name, other.name);
    }

    @Override
    // method to generate a hash code for ANode objects
    public int hashCode() {
        return Objects.hash(conceptId, name);
    }

    // method to get the concept ID of the ANode
    public Collection<String> getFileIds() {
        return this.fileIds;
    }
}

