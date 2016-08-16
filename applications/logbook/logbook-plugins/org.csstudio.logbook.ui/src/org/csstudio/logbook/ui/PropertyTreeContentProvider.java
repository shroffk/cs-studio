/**
 *
 */
package org.csstudio.logbook.ui;

import java.util.List;
import java.util.stream.Collectors;

import org.csstudio.logbook.Property;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * @author shroffk
 *
 */
public class PropertyTreeContentProvider implements ITreeContentProvider {

    private List<Property> properties;


    @Override
    public void dispose() {
    }


    @SuppressWarnings("unchecked")
    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        if(newInput != null){
            this.properties = ((List<Property>) newInput)
                                        .stream()
                                        .sorted((o1, o2) -> {return o1.getName().compareTo(o2.getName());})
                                        .collect(Collectors.toList());
        } else{
            this.properties = (List<Property>) newInput;
        }
    
    }

    @Override
    public Object[] getElements(Object inputElement) {
    return properties.toArray();
    }

    @Override
    public Object[] getChildren(Object parentElement) {
    if (parentElement instanceof Property) {
        return ((Property) parentElement).getAttributes().stream().sorted((o1, o2) -> {
            return o1.getKey().compareTo(o2.getKey());
        }).toArray();
    }
    return null;
    }

    @Override
    public Object getParent(Object element) {
    return null;
    }

    @Override
    public boolean hasChildren(Object element) {
    if (element instanceof Property) {
        return !((Property) element).getAttributes().isEmpty();
    }
    return false;
    }

}
