/*
 * Copyright © 2014.
 * This file is part of Friendscraft2 Launcher.
 * Friendscraft2 Launcher is licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.fc2.util;

public final class Parallel {

    private Parallel() {
        throw new RuntimeException("Use Parallel static methods");
    }

    /**
     * First-order function interface
     *
     * @param <E> Element to transform
     * @param <V> Result of the transformation
     * @author Pablo Rodríguez Mier
     */

    public static interface F<E, V> {
        /**
         * Apply a function over the element e.
         *
         * @param e Input element
         * @return transformation result
         */
        V apply(E e);
    }

    /**
     * Action class can be used to define a concurrent task that does not return
     * any value after processing the element.
     *
     * @param <E> Element processed within the action
     */
    public static abstract class Action<E> implements F<E, Void> {

        /**
         * This method is final and cannot be overridden. It applies the action
         * implemented by {@link Action#doAction(Object)}.
         */
        public final Void apply(E element) {
            doAction(element);
            return null;
        }

        /**
         * Defines the action that will be applied over the element. Every
         * action must implement this method.
         *
         * @param element element to process
         */
        public abstract void doAction(E element);
    }
}
