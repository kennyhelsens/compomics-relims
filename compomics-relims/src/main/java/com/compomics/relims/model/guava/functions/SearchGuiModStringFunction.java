package com.compomics.relims.model.guava.functions;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import javax.annotation.Nullable;
import java.util.List;

/**
 * This class is a
 */
public class SearchGuiModStringFunction implements Function<List<String>, String>{
    /**
     * Returns the result of applying this function to {@code input}. This method is <i>generally
     * expected</i>, but not absolutely required, to have the following properties:
     * <p/>
     * <ul>
     * <li>Its execution does not cause any observable side effects.
     * <li>The computation is <i>consistent with equals</i>; that is, {@link com.google.common.base.Objects#equal
     * Objects.equal}{@code (a, b)} implies that {@code Objects.equal(function.apply(a),
     * function.apply(b))}.
     * </ul>
     *
     * @throws NullPointerException if {@code input} is null and this function does not accept null
     *                              arguments
     */
    public String apply(@Nullable List<String> aPTMList) {
        List<String> aPTMNameList = Lists.transform(aPTMList, new Function<String, String>() {
            public String apply(@Nullable String input) {
                return input.toLowerCase();
            }
        });
        return Joiner.on("//").join(aPTMNameList);
    }
}
