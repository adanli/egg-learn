package com.egg.integration.eggwebflux.lambda;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Consumer
 * Predicate
 * Function
 * Supplier
 */
public class PracticeLambda {
    private static final Logger logger = LoggerFactory.getLogger(PracticeLambda.class);

    public static void main(String[] args) throws Exception{
        Class clazz = Class.forName("reactor.core.publisher.Mono");
        Method[] methods = clazz.getMethods();
        for(int i=0; i< methods.length; i++) {
            StringBuffer sb = new StringBuffer();
            Method method = methods[i];
            if(method.getModifiers() == Modifier.PUBLIC || method.getModifiers()==(Modifier.STATIC|Modifier.PUBLIC)) {
                Class returnClazz = method.getReturnType();
                sb.append(returnClazz.getSimpleName()).append(" ");
                String className = method.getName();
                sb.append(className).append("(");
                for(int j=0; j< method.getParameterCount(); j++) {
                    Parameter parameter = method.getParameters()[j];
                    Class paramType = parameter.getType();
                    String paramName = parameter.getName();
                    sb.append(paramType.getSimpleName()).append(" ").append(paramName);
                    if(j != method.getParameterCount()-1) {
                        sb.append(", ");
                    }

                }
                sb.append(")");
                sb.append(method.getModifiers());
            } else {
                continue;
            }

            logger.info("{}", sb.toString());

        }


    }

    public static Function<Integer, String> trans = (p) -> {
        return String.valueOf(p);
    };

    public static Consumer<Integer> print = (n) -> System.out.println(n);

    public static Predicate<Integer> moreThan = (n) -> n>5;

    public static Supplier<Integer> s  = () -> Integer.valueOf(10);

}
