/*
 * Copyright 2017 United States Government as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All Rights Reserved.
 * 
 * This file is available under the terms of the NASA Open Source Agreement
 * (NOSA). You should have received a copy of this agreement with the
 * Kepler source code; see the file NASA-OPEN-SOURCE-AGREEMENT.doc.
 * 
 * No Warranty: THE SUBJECT SOFTWARE IS PROVIDED "AS IS" WITHOUT ANY
 * WARRANTY OF ANY KIND, EITHER EXPRESSED, IMPLIED, OR STATUTORY,
 * INCLUDING, BUT NOT LIMITED TO, ANY WARRANTY THAT THE SUBJECT SOFTWARE
 * WILL CONFORM TO SPECIFICATIONS, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR FREEDOM FROM
 * INFRINGEMENT, ANY WARRANTY THAT THE SUBJECT SOFTWARE WILL BE ERROR
 * FREE, OR ANY WARRANTY THAT DOCUMENTATION, IF PROVIDED, WILL CONFORM
 * TO THE SUBJECT SOFTWARE. THIS AGREEMENT DOES NOT, IN ANY MANNER,
 * CONSTITUTE AN ENDORSEMENT BY GOVERNMENT AGENCY OR ANY PRIOR RECIPIENT
 * OF ANY RESULTS, RESULTING DESIGNS, HARDWARE, SOFTWARE PRODUCTS OR ANY
 * OTHER APPLICATIONS RESULTING FROM USE OF THE SUBJECT SOFTWARE.
 * FURTHER, GOVERNMENT AGENCY DISCLAIMS ALL WARRANTIES AND LIABILITIES
 * REGARDING THIRD-PARTY SOFTWARE, IF PRESENT IN THE ORIGINAL SOFTWARE,
 * AND DISTRIBUTES IT "AS IS."
 * 
 * Waiver and Indemnity: RECIPIENT AGREES TO WAIVE ANY AND ALL CLAIMS
 * AGAINST THE UNITED STATES GOVERNMENT, ITS CONTRACTORS AND
 * SUBCONTRACTORS, AS WELL AS ANY PRIOR RECIPIENT. IF RECIPIENT'S USE OF
 * THE SUBJECT SOFTWARE RESULTS IN ANY LIABILITIES, DEMANDS, DAMAGES,
 * EXPENSES OR LOSSES ARISING FROM SUCH USE, INCLUDING ANY DAMAGES FROM
 * PRODUCTS BASED ON, OR RESULTING FROM, RECIPIENT'S USE OF THE SUBJECT
 * SOFTWARE, RECIPIENT SHALL INDEMNIFY AND HOLD HARMLESS THE UNITED
 * STATES GOVERNMENT, ITS CONTRACTORS AND SUBCONTRACTORS, AS WELL AS ANY
 * PRIOR RECIPIENT, TO THE EXTENT PERMITTED BY LAW. RECIPIENT'S SOLE
 * REMEDY FOR ANY SUCH MATTER SHALL BE THE IMMEDIATE, UNILATERAL
 * TERMINATION OF THIS AGREEMENT.
 */

package gov.nasa.kepler.ar.exporter;

import gov.nasa.kepler.common.Cadence.CadenceType;
import gov.nasa.kepler.hibernate.cal.CalCrud;
import gov.nasa.kepler.hibernate.cal.UncertaintyTransformationMetadata;
import gov.nasa.kepler.hibernate.pi.PipelineInstance;
import gov.nasa.kepler.hibernate.pi.PipelineTask;
import gov.nasa.kepler.hibernate.pi.PipelineTaskCrud;

import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;

/**
 * Retrieves metadata about the front end of the science processing pipeline.
 * 
 * @author Miles Cote
 * 
 */
public class FrontEndPipelineMetadata {

    private final CalCrud calCrud;
    private final PipelineTaskCrud pipelineTaskCrud;

    public FrontEndPipelineMetadata() {
        this.calCrud = new CalCrud();
        this.pipelineTaskCrud = new PipelineTaskCrud();
    }

    public FrontEndPipelineMetadata(CalCrud calCrud,
        PipelineTaskCrud pipelineTaskCrud) {
        this.calCrud = calCrud;
        this.pipelineTaskCrud = pipelineTaskCrud;
    }

    /**
     * Retrieves a {@link PipelineInstance} that processed some of the data in
     * the front end of the pipeline for the input cadence range.
     */
    public PipelineInstance getPipelineInstance(CadenceType cadenceType,
        int startCadence, int endCadence) {
        List<UncertaintyTransformationMetadata> metadataList = calCrud.retrieveUncertaintyTransformationMetadata(
            startCadence, endCadence, cadenceType);
        if (metadataList.isEmpty()) {
            throw new IllegalArgumentException(
                "UncertaintyTransformationMetadata must exist in the database.\n  cadenceType: "
                    + cadenceType
                    + "\n  startCadence: "
                    + startCadence
                    + "\n  endCadence: " + endCadence);
        }

        NavigableSet<Long> pipelineTaskIds = new TreeSet<Long>();
        for (UncertaintyTransformationMetadata metadata : metadataList) {
            pipelineTaskIds.add(metadata.getPipelineTaskId());
        }

        PipelineTask largestPipelineTask = pipelineTaskCrud.retrieve(pipelineTaskIds.pollLast());

        return largestPipelineTask.getPipelineInstance();
    }

}