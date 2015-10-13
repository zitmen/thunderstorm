package cz.cuni.lf1.lge.ThunderSTORM.util;

import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.validators.Validator;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.validators.ValidatorException;

public class RangeValidatorFactory {

    public static Validator<String> fromTo() {
        return new Validator<String>() {
            @Override
            public void validate(String input) throws ValidatorException {
                try {
                    Range r = Range.parseFromTo(input);
                } catch(RuntimeException e) {
                    throw new ValidatorException(e.getMessage());
                }
            }
        };
    }

    public static Validator<String> fromStepTo() {
        return new Validator<String>() {
            @Override
            public void validate(String input) throws ValidatorException {
                try {
                    Range r = Range.parseFromStepTo(input);
                } catch(RuntimeException e) {
                    throw new ValidatorException(e.getMessage());
                }
            }
        };
    }

}
