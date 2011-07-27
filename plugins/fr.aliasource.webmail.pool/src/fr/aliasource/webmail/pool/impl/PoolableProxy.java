/* ***** BEGIN LICENSE BLOCK *****
 * Version: GPL 2.0
 *
 * The contents of this file are subject to the GNU General Public
 * License Version 2 or later (the "GPL").
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Initial Developer of the Original Code is
 *   MiniG.org project members
 *
 * ***** END LICENSE BLOCK ***** */

package fr.aliasource.webmail.pool.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import fr.aliasource.webmail.pool.IPoolable;
import fr.aliasource.webmail.pool.Pool;

public final class PoolableProxy<T extends IPoolable> implements
		InvocationHandler {

	private static final String DESTROY_METHOD = "destroy";

	private T proxied;
	private Pool<T> pool;

	private String destructor;

	public PoolableProxy(Pool<T> p, T poolable, String destroyMethod) {
		this.proxied = poolable;
		this.pool = p;
		this.destructor = destroyMethod;
	}

	public PoolableProxy(Pool<T> p, T poolable) {
		this(p, poolable, DESTROY_METHOD);
	}

	@SuppressWarnings("unchecked")
	public static <E extends IPoolable> E createProxy(Pool<E> p, E obj,
			String destroyMethod) {
		return (E) Proxy.newProxyInstance(PoolableProxy.class.getClassLoader(),
				obj.getClass().getInterfaces(), new PoolableProxy<E>(p, obj,
						destroyMethod));
	}

	@Override
	public boolean equals(Object obj) {
		return proxied.equals(obj);
	}

	@Override
	public int hashCode() {
		return proxied.hashCode();
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		if (!destructor.equals(method.getName())) {
			return method.invoke(proxied, args);
		} else {
			pool.reclaim(proxied);
			return null;
		}
	}

}
