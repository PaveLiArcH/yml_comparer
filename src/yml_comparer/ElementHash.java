/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package yml_comparer;

import java.util.Objects;

/**
 *
 * @author paveliarch
 */
public class ElementHash implements Comparable<ElementHash> {

    protected String element;
    protected long hash;

    public ElementHash(String element, long hash) {
        this.element = element;
        this.hash = hash;
    }

    @Override
    public int hashCode() {
        int _hash = 7;
        _hash = 61 * _hash + Objects.hashCode(this.element);
        _hash = 61 * _hash + (int) (this.hash ^ (this.hash >>> 32));
        return _hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ElementHash other = (ElementHash) obj;
        if (!Objects.equals(this.element, other.element)) {
            return false;
        }
        if (this.hash != other.hash) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(ElementHash t) {
        int _result = element.compareTo(t.element);
        if (_result == 0) {
            return Long.compare(hash, t.hash);
        }
        return _result;
    }
}
