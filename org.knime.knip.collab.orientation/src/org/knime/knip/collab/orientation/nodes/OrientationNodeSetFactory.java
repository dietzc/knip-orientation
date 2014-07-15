package org.knime.knip.collab.orientation.nodes;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSetFactory;
import org.knime.core.node.config.ConfigRO;
import org.knime.knip.collab.orientation.nodes.crossings.DecomposeJunctionsNodeFactory;
import org.knime.knip.collab.orientation.nodes.measure.OrientationMeasurementNodeFactory;
import org.knime.knip.collab.orientation.nodes.thinning.ThinningNodeFactory;

/**
 * 
 * @author dietzc, hornm (University of Konstanz)
 */
public class OrientationNodeSetFactory implements NodeSetFactory {

	private Map<String, String> m_nodeFactories = new HashMap<String, String>();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<String> getNodeFactoryIds() {

		m_nodeFactories.put(
				DecomposeJunctionsNodeFactory.class.getCanonicalName(),
				"/community/knip/projects/orientation");

		m_nodeFactories.put(ThinningNodeFactory.class.getCanonicalName(),
				"/community/knip/projects/orientation");

		m_nodeFactories.put(
				OrientationMeasurementNodeFactory.class.getCanonicalName(),
				"/community/knip/projects/orientation");

		return m_nodeFactories.keySet();
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends NodeFactory<? extends NodeModel>> getNodeFactory(
			final String id) {
		try {
			return (Class<? extends NodeFactory<? extends NodeModel>>) Class
					.forName(id);
		} catch (final ClassNotFoundException e) {
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getCategoryPath(final String id) {
		return m_nodeFactories.get(id);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getAfterID(final String id) {
		return "/";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ConfigRO getAdditionalSettings(final String id) {
		return null;
	}

}
