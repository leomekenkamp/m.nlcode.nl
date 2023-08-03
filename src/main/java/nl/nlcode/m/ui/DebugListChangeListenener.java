package nl.nlcode.m.ui;

import java.lang.invoke.MethodHandles;
import javafx.collections.ListChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author leo
 */
public class DebugListChangeListenener implements ListChangeListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public void onChanged(Change change) {
        LOGGER.info("start");
        while (change.next()) {
            LOGGER.info("next change");
            LOGGER.info("list: {}", change.getList());
            LOGGER.info("permutated: {}, updated: {}, replaced: {}, added: {}, removed: {}, ", change.wasPermutated(), change.wasUpdated(), change.wasReplaced(), change.wasAdded(), change.wasRemoved());
            LOGGER.info("from: {}, to: {}", change.getFrom(), change.getTo());
            LOGGER.info("removed size:{}, list: {}", change.getRemovedSize(), change.getRemoved());
            LOGGER.info("added size: {}, subList: {}", change.getAddedSize(), change.getAddedSubList());
            if (change.wasPermutated()) {
                for (int i = change.getFrom(); i <= change.getTo(); i++) {
                    LOGGER.info("permutation {}: {}", i, change.getPermutation(0));
                }
            }
        }
        LOGGER.info("end");
    }

}
