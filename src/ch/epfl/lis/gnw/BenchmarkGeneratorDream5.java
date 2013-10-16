/*
Copyright (c) 2008-2010 Daniel Marbach & Thomas Schaffter

We release this software open source under an MIT license (see below). If this
software was useful for your scientific work, please cite our paper(s) listed
on http://gnw.sourceforge.net.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

package ch.epfl.lis.gnw;




/** 
 * Class used to generate the DREAM5 network inference challenge
 * 
 * @author Daniel Marbach (firstname.name@gmail.com)
 */
public class BenchmarkGeneratorDream5 extends BenchmarkGenerator {

	/** A compendium of experiments */
	//private Compendium compendium_ = null;	
	/** Set true to anonymize the compendia */
	private boolean anonymize_ = false;
	
	/** The fraction of decoy genes to add (used for in vivo compendia) */
	private double fractionDecoyGenes_ = 0;
	/** The fraction of decoy TFs to add (used for in vivo compendia) */
	private double fractionDecoyTfs_ = 0;
	/** The fraction of random interaction to add (used only for the ecoli in silico compendium) */
	private double fractionRandomInteractions_ = 0;
	
	
	// ============================================================================
	// PUBLIC METHODS

	/** Default constructor */
    public BenchmarkGeneratorDream5() { 
    	super(null);
	}

    /** Constructor */
	public BenchmarkGeneratorDream5(GeneNetwork grn) {
		super(grn);
	}

	
	// ----------------------------------------------------------------------------

	/** Generate the benchmarks */
	public void generateBenchmark() throws Exception {
		
		anonymize_ = true;
		
		generateEcoliInsilicoCompendium("net1");
		//generateStaphCompendium("net2");
		//generateEcoliCompendium("net3");
		//generateYeastCompendium("net4");
		
		//generateTestInsilicoCompendium("test");
		//generateTestDream4InsilicoCompendium("test_dream4");

	}
	
	// ----------------------------------------------------------------------------

	/** Generate a compendium based on real data */
    public void generateInvivoBenchmark(String name, 
			String expressionFile,
			String experimentFile,
			String experimentDefFile,
			String geneListFile,
			String tfListFile,
			String goldStandardFile) {
    	
		// Create the compendium and load data
		Compendium compendium = new Compendium(name,
					expressionFile,
					experimentFile,
					experimentDefFile,
					geneListFile, 
					tfListFile,
					goldStandardFile);
		
		compendium.setFractionDecoyTfs(fractionDecoyTfs_);
		compendium.setFractionDecoyGenes(fractionDecoyGenes_);
		
		compendium.generateBenchmark(anonymize_);
    }
    
    
	// ----------------------------------------------------------------------------

    /** Generate an insilico compendium based on the given experiment definitions */
    public void generateInsilicoBenchmark(String name, 
			String experimentFile,
			String experimentDefFile,
			String goldStandardFile) {
    	
		// Create the compendium and load data
		CompendiumInsilico compendium = new CompendiumInsilico(name,
				experimentFile,
				experimentDefFile,
				goldStandardFile);
		
		compendium.setFractionDecoyTfs(fractionDecoyTfs_);
		compendium.setFractionDecoyGenes(fractionDecoyGenes_);
		compendium.setFractionRandomInteractions(fractionRandomInteractions_);
		
		// Generate model, run experiments, write data
		compendium.generateInSilicoBenchmark(anonymize_);

    }

    
	// ----------------------------------------------------------------------------

	/** Generate the DREAM5 ecoli in-silico benchmark */
	public void generateEcoliInsilicoCompendium(String name) {
		
		fractionDecoyGenes_ = 0.05;
		fractionDecoyTfs_ = 0.1;
		fractionRandomInteractions_ = 0.1;

		generateInsilicoBenchmark(name,
				"dream5/ecoli/ecoli_experiments.tsv",
				"dream5/ecoli/ecoli_insilico_experiment_defs.tsv",
				"dream5/ecoli/ecoli_transcriptional_network_regulonDB_6_7.tsv");
	}

	
	// ----------------------------------------------------------------------------

	/** Generate the DREAM5 ecoli in-silico benchmark */
	public void generateTestDream4InsilicoCompendium(String name) {
		
		fractionDecoyGenes_ = 0;
		fractionDecoyTfs_ = 0;
		fractionRandomInteractions_ = 0;
		anonymize_ = false;

		generateInsilicoBenchmark(name,
				"dream5/test/dream4/test_experiments.tsv",
				"dream5/test/dream4/test_experiment_defs.tsv",
				"dream5/test/dream4/insilico_size100_5.xml");
	}

	
	// ----------------------------------------------------------------------------

	/** Generate the DREAM5 ecoli benchmark */
	public void generateEcoliCompendium(String name) {
		
		fractionDecoyGenes_ = 0.05;
		fractionDecoyTfs_ = 0.1;
		
		generateInvivoBenchmark(name,
				"dream5/ecoli/ecoli_data.tsv",
				"dream5/ecoli/ecoli_experiments.tsv",
				"dream5/ecoli/ecoli_experiment_defs.tsv",
				"dream5/ecoli/ecoli_gene_names.tsv",
				"dream5/ecoli/ecoli_tf_names.tsv",
				"dream5/ecoli/ecoli_transcriptional_network_regulonDB_6_7.tsv");
	}

	
	// ----------------------------------------------------------------------------

	/** Generate the DREAM5 yeast benchmark */
	public void generateYeastCompendium(String name) {
		
		fractionDecoyGenes_ = 0.05;
		fractionDecoyTfs_ = 0;
		
		generateInvivoBenchmark(name,
				"dream5/yeast/yeast_data.tsv",
				"dream5/yeast/yeast_experiments.tsv",
				"dream5/yeast/yeast_experiment_defs.tsv",
				"dream5/yeast/yeast_gene_names.tsv",
				"dream5/yeast/yeast_tf_names.tsv",
				null);
	}

	
	// ----------------------------------------------------------------------------

	/** Generate the DREAM5 staph benchmark */
	public void generateStaphCompendium(String name) {
		
		fractionDecoyGenes_ = 0.05;
		fractionDecoyTfs_ = 0.1;
		
		generateInvivoBenchmark(name,
				"dream5/saureus/saureus_data.tsv",
				"dream5/saureus/saureus_experiments.tsv",
				"dream5/saureus/saureus_experiment_defs.tsv",
				"dream5/saureus/saureus_gene_names.tsv",
				"dream5/saureus/saureus_tf_names.tsv",
				null);
	}

	
	// ----------------------------------------------------------------------------

	/** 
	 * Run all experiments, save the gold standards and the datasets.
	 * @throws CancelException, Exception 
	 */
	public void generateTestCompendium(String name) {
		
		generateInvivoBenchmark(name,
					"dream5/test/test_data.tsv", 
					"dream5/test/test_experiments.tsv",
					"dream5/test/test_experiment_defs.tsv",
					"dream5/test/test_gene_names.tsv", 
					"dream5/test/test_tf_names.tsv",
					"dream5/test/test_gold_standard.tsv");
	}

	
	// ----------------------------------------------------------------------------

	/** 
	 * Run all experiments, save the gold standards and the datasets.
	 * @throws CancelException, Exception 
	 */
	public void generateTestInsilicoCompendium(String name) {
		
		generateInsilicoBenchmark(name,
				"dream5/test/test_experiments.tsv",
				"dream5/test/test_experiment_defs.tsv",
				"dream5/test/ecoli/ecoli_transcriptional_network_regulonDB_6_7.tsv");
		
		//generateInsilicoBenchmark(name,
			//		"dream5/test/multifact/drug_ss_experiments.tsv",
				//	"dream5/test/multifact/drug_ss_experiment_defs.tsv",
					//"dream5/test/multifact/ecoli_transcriptional_network_regulonDB_6_7.tsv");
		
	}

		
	// ============================================================================
	// PRIVATE METHODS


	
	// ============================================================================
	// SETTERS AND GETTERS

	public void setAnonymize(boolean anonymize) { anonymize_ = anonymize; }

	
}
