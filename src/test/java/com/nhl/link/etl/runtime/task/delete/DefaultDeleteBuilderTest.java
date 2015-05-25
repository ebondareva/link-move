package com.nhl.link.etl.runtime.task.delete;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.map.EntityResolver;
import org.apache.cayenne.map.ObjAttribute;
import org.apache.cayenne.map.ObjEntity;
import org.junit.Before;
import org.junit.Test;

import com.nhl.link.etl.runtime.cayenne.ITargetCayenneService;
import com.nhl.link.etl.runtime.extract.IExtractorService;
import com.nhl.link.etl.runtime.key.IKeyAdapterFactory;
import com.nhl.link.etl.runtime.task.ITaskService;
import com.nhl.link.etl.runtime.task.sourcekeys.DefaultSourceKeysBuilder;
import com.nhl.link.etl.runtime.token.ITokenManager;
import com.nhl.link.etl.unit.cayenne.t.Etl1t;

public class DefaultDeleteBuilderTest {

	private DefaultDeleteBuilder<Etl1t> builder;

	@Before
	public void before() {

		ObjAttribute matchAttribute = new ObjAttribute("abc");
		matchAttribute.setType(Object.class.getName());

		ObjEntity targetEntity = new ObjEntity();
		targetEntity.addAttribute(matchAttribute);

		EntityResolver resolver = mock(EntityResolver.class);
		when(resolver.getObjEntity(any(Class.class))).thenReturn(targetEntity);

		ITargetCayenneService cayenneService = mock(ITargetCayenneService.class);
		when(cayenneService.entityResolver()).thenReturn(resolver);

		IKeyAdapterFactory keyAdapterFactory = mock(IKeyAdapterFactory.class);

		ITaskService taskService = mock(ITaskService.class);
		when(taskService.extractSourceKeys()).thenReturn(
				new DefaultSourceKeysBuilder(mock(IExtractorService.class), mock(ITokenManager.class),
						keyAdapterFactory));

		builder = new DefaultDeleteBuilder<>(Etl1t.class, cayenneService, keyAdapterFactory, taskService);
	}

	@Test(expected = IllegalStateException.class)
	public void testTask_NoExtractorName() {
		builder.task();
	}

	@Test
	public void test_MinimalConfig() {

		DeleteTask<Etl1t> task = builder.sourceMatchExtractor("x").matchBy("abc").task();
		assertNotNull(task);
		assertEquals(Etl1t.class, task.type);
		assertEquals(500, task.batchSize);
		assertNull(task.targetFilter);
		assertEquals("x", task.extractorName);
	}

	@Test
	public void testTargetFilter() {
		Expression e = Etl1t.NAME.eq("Joe");
		DeleteTask<Etl1t> task = builder.sourceMatchExtractor("x").matchBy("abc").targetFilter(e).task();
		assertSame(e, task.targetFilter);
	}

	@Test
	public void testBatchSize() {
		DeleteTask<Etl1t> task = builder.sourceMatchExtractor("x").matchBy("abc").batchSize(55).task();
		assertEquals(55, task.batchSize);
	}
}