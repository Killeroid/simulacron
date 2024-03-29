package simulacron.strategy.reproduction;

import java.util.List;
import java.util.ArrayList;

import simulacron.model.Simulator;
import simulacron.model.Service;


/**
 * This class implements the Reduction speciation strategy
 * @author Vivek Nallur
 */

public class DNAReductionSpeciation implements DNASpeciation{
        public List<Service> speciate(List<Service> current_dna, List<Service> all_services){
			ArrayList<Service> current_services = new ArrayList<Service> (current_dna);
			current_services.remove(Simulator.INSTANCE.random.nextInt(current_services.size()));
			return new ArrayList<Service> (current_services);
        }
}
