package simulacron.strategy.reproduction;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

import simulacron.model.Simulator;
import simulacron.model.Service;


/**
 * This class implements the Extension speciation strategy
 * @author Vivek Nallur
 */

public class DNAExtensionSpeciation implements DNASpeciation{
        public List<Service> speciate(List<Service> current_dna, List<Service> all_services){
			if(current_dna.size() == Simulator.INSTANCE.getInitServices())
				return new ArrayList<Service> (current_dna);
        	Set<Service> current_services = new HashSet<Service>(current_dna);
			Service new_service;
			do {
				new_service = all_services.get(Simulator.INSTANCE.random.nextInt(all_services.size()));
			}while(!current_services.add(new_service));
			return new ArrayList<Service> (current_services);
        }
}
